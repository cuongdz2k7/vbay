package com.vbay.ui.util;

import com.vbay.shared.Utils.LoggingUtils;
import javafx.scene.media.AudioClip;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MusicManager {
    private static final Logger LOGGER = LoggingUtils.getLogger(MusicManager.class);

    public static void playSound(String soundFileName) {
        try {
            java.net.URL resource = MusicManager.class.getResource("/audio/" + soundFileName);
            if (resource == null) {
                LOGGER.warning("Could not find sound file: " + soundFileName);
                return;
            }

            String soundUrl = resource.toExternalForm();
            AudioClip audioClip = new AudioClip(soundUrl);
            audioClip.play();
            LOGGER.info("Successfully played sound effect: " + soundFileName);
        } catch (Exception exception) {
            LOGGER.log(Level.SEVERE, "Could not play sound effect " + soundFileName + ": ", exception);
        }
    }
}
