use thiserror::Error;

#[derive(Error, Debug)]
pub enum AudioEngineError {
    #[error("IO error: {0}")]
    Io(#[from] std::io::Error),

    #[error("Symphonia error: {0}")]
    Symphonia(#[from] symphonia::core::errors::Error),

    #[error("No audio track found in media container")]
    NoTrackFound,

    #[error("Primary audio format failed and no valid fallback was available: {0}")]
    PlaybackFailed(String),

    #[error("Decoder initialization error: {0}")]
    DecoderInit(String),
}
