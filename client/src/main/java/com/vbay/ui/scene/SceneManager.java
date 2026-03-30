package com.vbay.ui.scene;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
public class SceneManager {
    private static Stage currentStage;

    public static void setStage(Stage new_stage){
        currentStage = new_stage;
    } 

    public static void switchScene(String fxmlPath) throws Exception{ // Path tuyệt đối , ví dụ: " /jfx/scene/Authentication.fxml"

        Parent root = FXMLLoader.load(Objects.requireNonNull(SceneManager.class.getResource(fxmlPath)));
        currentStage.setScene(new Scene(root));
        currentStage.show();
    }
    public static Stage getStage(){
        return currentStage;
    }

}
