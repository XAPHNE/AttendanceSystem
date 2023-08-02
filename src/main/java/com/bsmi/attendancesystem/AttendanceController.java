package com.bsmi.attendancesystem;

import io.github.palexdev.materialfx.controls.MFXButton;
import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AttendanceController {
    @FXML
    private MFXButton closeMFXButton;
    @FXML
    private Label errorMessage;
    @FXML
    private MFXTextField usernameMFXTextField;
    @FXML
    private MFXPasswordField passwordMFXPasswordField;

    @FXML
    public void closeMFXButtonOnAction(ActionEvent e){
        Stage stage = (Stage) closeMFXButton.getScene().getWindow();
        stage.close();
    }
    @FXML
    protected void loginButtonOnAction() {
        if (!usernameMFXTextField.getText().isBlank() && !passwordMFXPasswordField.getText().isBlank()){
            //errorMessage.setText("You try to login!");
            validateLogin();
        } else {
            errorMessage.setText("Please enter Username and Password");
        }
    }

    public void validateLogin() {
        DatabaseConnection connectNow = new DatabaseConnection();
        Connection connectDB = connectNow.getConnection();
        String verifyLogin = "SELECT count(1) FROM useraccounts WHERE username = '" + usernameMFXTextField.getText() + "' AND password = '" + passwordMFXPasswordField.getText() + "'";

        try {
            Statement statement = connectDB.createStatement();
            ResultSet queryResult = statement.executeQuery(verifyLogin);

            while (queryResult.next()) {
                if (queryResult.getInt(1) == 1) {
                    errorMessage.setText("Welcome!");
                } else {
                    errorMessage.setText("Invalid Login. Please try again.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}