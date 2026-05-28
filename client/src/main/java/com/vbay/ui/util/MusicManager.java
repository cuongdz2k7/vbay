package com.vbay.ui.util;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.vbay.shared.Utils.LoggingUtils;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;

public class MusicManager {
    private static final Logger LOGGER = LoggingUtils.getLogger(MusicManager.class);
    private static MediaPlayer mediaPlayer;
    private static boolean isMuted = false;
    private static double currentVolume = 0.3; // Default pleasant 30% volume

    public static void playBackgroundMusic() {
        try {
            // Find the music file in the "audio" folder at the root or relative paths
            File file = new File("audio/Noi_Nay_Co_Anh_Audio.mp3");
            if (!file.exists()) {
                file = new File("../audio/Noi_Nay_Co_Anh_Audio.mp3");
            }
            if (!file.exists()) {
                LOGGER.warning("Could not find Noi_Nay_Co_Anh_Audio.mp3 inside audio/ folder.");
                return;
            }

            String musicUrl = file.toURI().toString();
            Media media = new Media(musicUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(currentVolume);
            mediaPlayer.setMute(isMuted);
            mediaPlayer.play();
            LOGGER.info("Successfully started background music: " + file.getName());
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Could not start background music: ", exception);
        }
    }

    public static void playSound(String soundFileName) {
        if (isMuted) {
            return;
        }
        try {
            File file = new File("audio/" + soundFileName);
            if (!file.exists()) {
                file = new File("../audio/" + soundFileName);
            }
            if (!file.exists()) {
                LOGGER.warning("Could not find sound file: " + soundFileName);
                return;
            }

            String soundUrl = file.toURI().toString();
            AudioClip audioClip = new AudioClip(soundUrl);
            audioClip.play();
            LOGGER.info("Successfully played sound effect: " + soundFileName);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Could not play sound effect " + soundFileName + ": ", exception);
        }
    }

    public static void setVolume(double volume) {
        currentVolume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }

    public static double getVolume() {
        return currentVolume;
    }

    public static void setMuted(boolean mute) {
        isMuted = mute;
        if (mediaPlayer != null) {
            mediaPlayer.setMute(mute);
        }
    }

    public static boolean isMuted() {
        return isMuted;
    }

    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
