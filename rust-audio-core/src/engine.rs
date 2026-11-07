use parking_lot::Mutex;
use std::sync::atomic::{AtomicBool, AtomicU64, Ordering};
use std::sync::Arc;
use std::time::Duration;

use crate::decoder::ResilientAudioSource;
use crate::error::AudioEngineError;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum PlaybackState {
    Idle,
    Buffering,
    Playing,
    Paused,
    Completed,
    Error,
}

pub struct AudioEngine {
    source: Mutex<Option<ResilientAudioSource>>,
    state: Mutex<PlaybackState>,
    position_ms: AtomicU64,
    volume: Mutex<f32>,
    is_active: AtomicBool,
}

impl AudioEngine {
    pub fn new() -> Arc<Self> {
        Arc::new(Self {
            source: Mutex::new(None),
            state: Mutex::new(PlaybackState::Idle),
            position_ms: AtomicU64::new(0),
            volume: Mutex::new(1.0),
            is_active: AtomicBool::new(false),
        })
    }

    pub fn load_track(
        &self,
        m4a_path: String,
        backup_mp3_path: Option<String>,
    ) -> Result<bool, AudioEngineError> {
        let mut source = ResilientAudioSource::new(m4a_path, backup_mp3_path);
        source.initialize()?;

        let was_failover = source.using_backup;
        *self.source.lock() = Some(source);
        *self.state.lock() = PlaybackState::Paused;
        self.position_ms.store(0, Ordering::SeqCst);
        self.is_active.store(true, Ordering::SeqCst);

        Ok(was_failover)
    }

    pub fn play(&self) {
        let mut state = self.state.lock();
        if *state == PlaybackState::Paused || *state == PlaybackState::Buffering {
            *state = PlaybackState::Playing;
        }
    }

    pub fn pause(&self) {
        let mut state = self.state.lock();
        if *state == PlaybackState::Playing {
            *state = PlaybackState::Paused;
        }
    }

    pub fn stop(&self) {
        let mut state = self.state.lock();
        *state = PlaybackState::Idle;
        self.position_ms.store(0, Ordering::SeqCst);
        self.is_active.store(false, Ordering::SeqCst);
    }

    pub fn get_state(&self) -> PlaybackState {
        *self.state.lock()
    }

    pub fn get_position_ms(&self) -> u64 {
        self.position_ms.load(Ordering::SeqCst)
    }

    pub fn set_position_ms(&self, ms: u64) {
        self.position_ms.store(ms, Ordering::SeqCst);
    }

    pub fn set_volume(&self, vol: f32) {
        *self.volume.lock() = vol.clamp(0.0, 1.0);
    }

    pub fn get_volume(&self) -> f32 {
        *self.volume.lock()
    }

    pub fn is_using_backup(&self) -> bool {
        self.source
            .lock()
            .as_ref()
            .map(|s| s.using_backup)
            .unwrap_or(false)
    }

    /// Audio render pump: reads samples from the active stream and advances position
    pub fn pump_samples(&self, max_samples: usize) -> Result<Option<Vec<f32>>, AudioEngineError> {
        if *self.state.lock() != PlaybackState::Playing {
            return Ok(None);
        }

        let mut source_lock = self.source.lock();
        if let Some(ref mut source) = *source_lock {
            match source.read_samples()? {
                Some(mut samples) => {
                    let vol = *self.volume.lock();
                    if (vol - 1.0).abs() > f32::EPSILON {
                        for s in samples.iter_mut() {
                            *s *= vol;
                        }
                    }
                    self.position_ms
                        .store(source.current_position_ms(), Ordering::SeqCst);

                    if samples.len() > max_samples {
                        samples.truncate(max_samples);
                    }
                    Ok(Some(samples))
                }
                None => {
                    *self.state.lock() = PlaybackState::Completed;
                    Ok(None)
                }
            }
        } else {
            Ok(None)
        }
    }
}
