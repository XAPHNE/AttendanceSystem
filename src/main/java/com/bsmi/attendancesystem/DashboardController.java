package com.bsmi.attendancesystem;

import io.github.palexdev.materialfx.controls.MFXButton;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class DashboardController {

    private Stage window;
    @FXML
    private MFXButton logoutMFXButton;

    @FXML
    public void logoutMFXButtonOnAction(ActionEvent event){
        window = (Stage) logoutMFXButton.getScene().getWindow();
        window.close();
        Stage stage = AttendanceApplication.getWindow();
        stage.show();
    }

    public void display(){
        Stage stage = AttendanceApplication.getWindow();
        stage.hide();
        window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/dashboard-view.fxml"));
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
