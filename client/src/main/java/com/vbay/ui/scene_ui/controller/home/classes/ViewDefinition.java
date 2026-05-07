package com.vbay.ui.scene_ui.controller.home.classes;

import javafx.scene.control.Button;

public final class ViewDefinition {
    public final String key;
    public final String moduleKey;
    public final String sectionName;
    public final Button button;
    public final String displayName;
    public final String subtitle;

    public ViewDefinition(
        String key,
        String moduleKey,
        String sectionName,
        Button button,
        String displayName,
        String subtitle
    ) {
        this.key = key;
        this.moduleKey = moduleKey;
        this.sectionName = sectionName;
        this.button = button;
        this.displayName = displayName;
        this.subtitle = subtitle;
    }
}
