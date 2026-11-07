use std::fs::File;
use std::path::{Path, PathBuf};
use symphonia::core::audio::{AudioBufferRef, SampleBuffer};
use symphonia::core::codecs::{Decoder, DecoderOptions, CODEC_TYPE_NULL};
use symphonia::core::errors::Error as SymphoniaError;
use symphonia::core::formats::{FormatOptions, FormatReader};
use symphonia::core::io::MediaSourceStream;
use symphonia::core::meta::MetadataOptions;
use symphonia::core::probe::Hint;

use crate::error::AudioEngineError;

pub struct StreamDecoder {
    format: Box<dyn FormatReader>,
    decoder: Box<dyn Decoder>,
    track_id: u32,
    sample_rate: u32,
    channels: usize,
    sample_buffer: Option<SampleBuffer<f32>>,
}

impl StreamDecoder {
    pub fn open<P: AsRef<Path>>(path: P) -> Result<Self, AudioEngineError> {
        let file = File::open(path.as_ref())?;
        let mss = MediaSourceStream::new(Box::new(file), Default::default());

        let mut hint = Hint::new();
        if let Some(ext) = path.as_ref().extension().and_then(|s| s.to_str()) {
            hint.with_extension(ext);
        }

        let format_opts = FormatOptions {
            enable_gapless: true,
            ..Default::default()
        };
        let meta_opts = MetadataOptions::default();

        let probed = symphonia::default::get_probe()
            .format(&hint, mss, &format_opts, &meta_opts)
            .map_err(AudioEngineError::Symphonia)?;

        let format = probed.format;

        let track = format
            .tracks()
            .iter()
            .find(|t| t.codec_params.codec != CODEC_TYPE_NULL)
            .ok_or(AudioEngineError::NoTrackFound)?;

        let track_id = track.id;
        let sample_rate = track.codec_params.sample_rate.unwrap_or(44100);
        let channels = track.codec_params.channels.map(|c| c.count()).unwrap_or(2);

        let dec_opts = DecoderOptions::default();
        let decoder = symphonia::default::get_codecs()
            .make(&track.codec_params, &dec_opts)
            .map_err(AudioEngineError::Symphonia)?;

        Ok(Self {
            format,
            decoder,
            track_id,
            sample_rate,
            channels,
            sample_buffer: None,
        })
    }

    pub fn next_packet_samples(&mut self) -> Result<Option<Vec<f32>>, AudioEngineError> {
        loop {
            let packet = match self.format.next_packet() {
                Ok(p) => p,
                Err(SymphoniaError::IoError(ref err))
                    if err.kind() == std::io::ErrorKind::UnexpectedEof =>
                {
                    return Ok(None);
                }
                Err(e) => return Err(AudioEngineError::Symphonia(e)),
            };

            if packet.track_id() != self.track_id {
                continue;
            }

            match self.decoder.decode(&packet) {
                Ok(decoded) => {
                    let mut sample_buf = match self.sample_buffer.take() {
                        Some(mut buf) => {
                            buf.copy_interleaved_ref(decoded);
                            buf
                        }
                        None => {
                            let spec = *decoded.spec();
                            let duration = decoded.capacity() as u64;
                            let mut buf = SampleBuffer::<f32>::new(duration, spec);
                            buf.copy_interleaved_ref(decoded);
                            buf
                        }
                    };

                    let samples = sample_buf.samples().to_vec();
                    self.sample_buffer = Some(sample_buf);
                    return Ok(Some(samples));
                }
                Err(SymphoniaError::DecodeError(_)) => {
                    // Recoverable frame error, continue to next frame
                    continue;
                }
                Err(e) => return Err(AudioEngineError::Symphonia(e)),
            }
        }
    }

    pub fn sample_rate(&self) -> u32 {
        self.sample_rate
    }

    pub fn channels(&self) -> usize {
        self.channels
    }
}

pub struct ResilientAudioSource {
    primary_path: PathBuf,
    backup_path: Option<PathBuf>,
    active_decoder: Option<StreamDecoder>,
    pub using_backup: bool,
    pub samples_rendered: u64,
}

impl ResilientAudioSource {
    pub fn new<P: AsRef<Path>>(primary: P, backup: Option<P>) -> Self {
        Self {
            primary_path: primary.as_ref().to_path_buf(),
            backup_path: backup.map(|p| p.as_ref().to_path_buf()),
            active_decoder: None,
            using_backup: false,
            samples_rendered: 0,
        }
    }

    pub fn initialize(&mut self) -> Result<(), AudioEngineError> {
        // Try opening primary M4A format first
        match StreamDecoder::open(&self.primary_path) {
            Ok(dec) => {
                self.active_decoder = Some(dec);
                self.using_backup = false;
                Ok(())
            }
            Err(e) => {
                // If primary fails, trigger invisible failover to MP3 backup if present
                if let Some(ref backup) = self.backup_path {
                    match StreamDecoder::open(backup) {
                        Ok(dec) => {
                            self.active_decoder = Some(dec);
                            self.using_backup = true;
                            Ok(())
                        }
                        Err(backup_err) => Err(AudioEngineError::PlaybackFailed(format!(
                            "Primary error: {e}, Backup error: {backup_err}"
                        ))),
                    }
                } else {
                    Err(e)
                }
            }
        }
    }

    pub fn read_samples(&mut self) -> Result<Option<Vec<f32>>, AudioEngineError> {
        if self.active_decoder.is_none() {
            self.initialize()?;
        }

        let decoder = self.active_decoder.as_mut().unwrap();
        match decoder.next_packet_samples() {
            Ok(Some(samples)) => {
                self.samples_rendered += (samples.len() / decoder.channels()) as u64;
                Ok(Some(samples))
            }
            Ok(None) => Ok(None),
            Err(e) => {
                // If primary playback fails mid-stream and backup is available, failover!
                if !self.using_backup {
                    if let Some(ref backup) = self.backup_path {
                        if let Ok(mut backup_dec) = StreamDecoder::open(backup) {
                            self.using_backup = true;
                            let res = backup_dec.next_packet_samples();
                            self.active_decoder = Some(backup_dec);
                            return res;
                        }
                    }
                }
                Err(e)
            }
        }
    }

    pub fn current_position_ms(&self) -> u64 {
        if let Some(ref dec) = self.active_decoder {
            if dec.sample_rate > 0 {
                return (self.samples_rendered * 1000) / dec.sample_rate as u64;
            }
        }
        0
    }
}
