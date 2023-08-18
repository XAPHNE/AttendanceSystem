package com.bsmi.attendancesystem;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class DashboardController {
    public void display(){
        Stage window = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(DashboardController.class.getResource("fxml/dashboard-view.fxml"));
        try {
            Scene scene = new Scene(fxmlLoader.load(), 1100, 600);
            window.initStyle(StageStyle.UNDECORATED);
            window.setScene(scene);
            window.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
