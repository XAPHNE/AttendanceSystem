package com.bsmi.attendancesystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class AttendanceApplication extends Application {

    private static Stage window;
    @Override
    public void start(Stage stage) throws IOException {
        window = stage;
        FXMLLoader fxmlLoader = new FXMLLoader(AttendanceApplication.class.getResource("fxml/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 938, 583);
        stage.initStyle(StageStyle.UNDECORATED);
        //stage.setTitle("Login");
        stage.setScene(scene);
        stage.show();
    }

    public static Stage getWindow(){
        return window;
    }

    public static void main(String[] args) {
        launch();
    }
}