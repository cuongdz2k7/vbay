package com.vbay.ui.scene_ui.controller.home.classes;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public final class ModuleSection {
    public final String key;
    public final Button button;
    public final VBox panel;

    public ModuleSection(String key, Button button, VBox panel) {
        this.key = key;
        this.button = button;
        this.panel = panel;
    }
}
