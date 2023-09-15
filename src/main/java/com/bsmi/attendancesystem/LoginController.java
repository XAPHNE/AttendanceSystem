package com.bsmi.attendancesystem;

import io.github.palexdev.materialfx.controls.MFXPasswordField;
import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.input.MouseEvent;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginController {
    @FXML
    public MFXTextField usernameMFXTextField;
    @FXML
    public MFXPasswordField passwordMFXPasswordField;
    @FXML
    public Button loginButton;
    @FXML
    private Button closeButton;

    private Connection connect;
    private PreparedStatement prepare;
    private ResultSet result;

    private double xOffset = 0;
    private double yOffset = 0;
    private Button home_btn;

    @FXML
    protected void loginButtonOnAction(){
        String sql = "SELECT * FROM admin WHERE username = ?";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            prepare = connect.prepareStatement(sql);
            prepare.setString(1, usernameMFXTextField.getText());
            result = prepare.executeQuery();

            // EMPTY FIELD CHECK
            if (usernameMFXTextField.getText().isEmpty() || passwordMFXPasswordField.getText().isEmpty()){
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Please fill all blank fields");
                alert.showAndWait();
            } else {
                if (result.next()) {
                    String storedHashedPassword = result.getString("password_hash"); // Retrieve hashed password from the database
                    String enteredPassword = passwordMFXPasswordField.getText();

                    if (BCrypt.checkpw(enteredPassword, storedHashedPassword)) { // Check if entered password matches the hashed password
                        // PROCEDING TO DASHBOARD FORM
                        GetData.username = usernameMFXTextField.getText();
                        GetData.userRole = result.getString("user_role");
                        alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Information Message");
                        alert.setHeaderText(null);
                        alert.setContentText("Login Successful");
                        alert.showAndWait();

                        // HIDING LOGIN FORM
                        loginButton.getScene().getWindow().hide();

                        // LINKING DASHBOARD
                        Parent root = FXMLLoader.load(getClass().getResource("fxml/dashboard-view.fxml"));
                        Stage stage = new Stage();
                        Scene scene = new Scene(root);
                        stage.setTitle("Dashboard | Student Management System");

                        // Make the window draggable
                        root.setOnMousePressed((MouseEvent event) -> {
                            xOffset = event.getSceneX();
                            yOffset = event.getSceneY();
                        });

                        root.setOnMouseDragged((MouseEvent event) -> {
                            stage.setX(event.getScreenX() - xOffset);
                            stage.setY(event.getScreenY() - yOffset);
                            stage.setOpacity(.6);
                        });

                        // Reset opacity on mouse release
                        root.setOnMouseReleased((MouseEvent event) -> {
                            stage.setOpacity(1);
                        });

                        stage.initStyle(StageStyle.TRANSPARENT);
                        stage.setScene(scene);
                        stage.show();
                    } else {
                        // ERROR MESSAGE WILL APPEAR
                        alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error Message");
                        alert.setHeaderText(null);
                        alert.setContentText("Incorrect Username/Password");
                        alert.showAndWait();
                    }
                } else {
                    // ERROR MESSAGE WILL APPEAR
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Incorrect Username/Password");
                    alert.showAndWait();
                }
            }
            result.close();
            prepare.close();
            connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void closeButtonOnAction(ActionEvent e) {
//        Stage stage = (Stage) closeButton.getScene().getWindow();
//        stage.close();
        System.exit(0);
    }
}
