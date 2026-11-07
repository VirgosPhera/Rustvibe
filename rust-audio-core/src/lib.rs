pub mod decoder;
pub mod engine;
pub mod error;

pub use engine::{AudioEngine, PlaybackState};
pub use error::AudioEngineError;

#[cfg(test)]
mod tests {
    use super::*;
    use std::io::Write;
    use tempfile::NamedTempFile;

    #[test]
    fn test_engine_initial_state() {
        let engine = AudioEngine::new();
        assert_eq!(engine.get_state(), PlaybackState::Idle);
        assert_eq!(engine.get_position_ms(), 0);
        assert_eq!(engine.get_volume(), 1.0);
        assert!(!engine.is_using_backup());
    }

    #[test]
    fn test_volume_clamp() {
        let engine = AudioEngine::new();
        engine.set_volume(1.5);
        assert_eq!(engine.get_volume(), 1.0);
        engine.set_volume(-0.5);
        assert_eq!(engine.get_volume(), 0.0);
        engine.set_volume(0.75);
        assert!((engine.get_volume() - 0.75).abs() < f32::EPSILON);
    }

    #[test]
    fn test_nonexistent_track_error() {
        let engine = AudioEngine::new();
        let res = engine.load_track("/path/to/nonexistent_track.m4a".into(), None);
        assert!(res.is_err());
        assert_eq!(engine.get_state(), PlaybackState::Idle);
    }

    #[test]
    fn test_corrupted_primary_with_no_backup_fails() {
        let engine = AudioEngine::new();
        let mut corrupt_m4a = NamedTempFile::new().unwrap();
        corrupt_m4a
            .write_all(b"not an audio file - corrupted header data")
            .unwrap();

        let path = corrupt_m4a.path().to_str().unwrap().to_string();
        let res = engine.load_track(path, None);
        assert!(res.is_err());
        assert!(!engine.is_using_backup());
    }

    #[test]
    fn test_corrupted_primary_with_corrupted_backup_fails() {
        let engine = AudioEngine::new();
        let mut corrupt_m4a = NamedTempFile::new().unwrap();
        corrupt_m4a.write_all(b"corrupted m4a header").unwrap();

        let mut corrupt_mp3 = NamedTempFile::new().unwrap();
        corrupt_mp3.write_all(b"corrupted mp3 header").unwrap();

        let m4a_path = corrupt_m4a.path().to_str().unwrap().to_string();
        let mp3_path = corrupt_mp3.path().to_str().unwrap().to_string();

        let res = engine.load_track(m4a_path, Some(mp3_path));
        assert!(res.is_err());
        match res.unwrap_err() {
            AudioEngineError::PlaybackFailed(msg) => {
                assert!(msg.contains("Primary error"));
                assert!(msg.contains("Backup error"));
            }
            other => panic!("Expected PlaybackFailed, got: {other:?}"),
        }
    }

    #[test]
    fn test_state_flow() {
        let engine = AudioEngine::new();
        engine.play();
        assert_eq!(engine.get_state(), PlaybackState::Idle); // Cannot play from Idle without track

        engine.set_position_ms(5420);
        assert_eq!(engine.get_position_ms(), 5420);

        engine.stop();
        assert_eq!(engine.get_position_ms(), 0);
        assert_eq!(engine.get_state(), PlaybackState::Idle);
    }
}
