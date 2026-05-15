package com.vbay.ui.scene_ui.controller.home.classes;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public final class SectionGroup {
    public final String moduleKey;
    public final String key;
    public final Button button;
    public final VBox submenu;

    public SectionGroup(String moduleKey, String key, Button button, VBox submenu) {
        this.moduleKey = moduleKey;
        this.key = key;
        this.button = button;
        this.submenu = submenu;
    }
}
