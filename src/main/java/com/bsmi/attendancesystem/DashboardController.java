package com.bsmi.attendancesystem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Stream;

public class DashboardController implements Initializable {
    @FXML
    private Button addStudent_btn;

    @FXML
    private DatePicker addStudent_birthDate;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_birthDate;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_course;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_firstName;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_gender;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_lastName;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_status;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_studentNum;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_year;

    @FXML
    private ComboBox<?> addStudent_course;

    @FXML
    private TextField addStudent_firstName;

    @FXML
    private AnchorPane addStudent_form;

    @FXML
    private ComboBox<?> addStudent_gender;

    @FXML
    private ImageView addStudent_imageView;

    @FXML
    private TextField addStudent_lastName;

    @FXML
    private TextField addStudent_search;

    @FXML
    private ComboBox<?> addStudent_status;

    @FXML
    private TextField addStudent_studentNum;

    @FXML
    private TableView<StudentData> addStudent_tableView;

    @FXML
    private ComboBox<?> addStudent_year;

    @FXML
    private Button availableCourse_addBtn;

    @FXML
    private Button availableCourse_clearBtn;

    @FXML
    private TableColumn<CourseData, String> availableCourse_col_course;

    @FXML
    private TableColumn<CourseData, String> availableCourse_col_degree;

    @FXML
    private TableColumn<CourseData, String> availableCourse_col_description;

    @FXML
    private TextField availableCourse_course;

    @FXML
    private TextField availableCourse_degree;

    @FXML
    private Button availableCourse_deleteBtn;

    @FXML
    private TextField availableCourse_description;

    @FXML
    private AnchorPane availableCourse_form;

    @FXML
    private TableView<CourseData> availableCourse_tableView;

    @FXML
    private Button availableCourse_updateBtn;

    @FXML
    private Button availableCourses_btn;

    @FXML
    private Button close_btn;

    @FXML
    private Button home_btn;

    @FXML
    private AnchorPane home_form;

    @FXML
    private Button logout_btn;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button minimize_btn;

    @FXML
    private Button studentAttendance_btn;

    @FXML
    private AnchorPane studentAttendance_form;

    @FXML
    private Label username;

    private double xOffset = 0;
    private double yOffset = 0;
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private Image image;
    private String[] yearList = {"1st Year", "2nd Year", "3rd Year", "4th Year", "Graduated"};
    private String[] genderList = {"Male", "Female", "Others"};
    private String[] statusList = {"Enrolled", "Graduated", "Dropped Out"};

//    START CODE FOR MINIMIZE BUTTON
    @FXML
    public void closeBtnOnAction() {
        System.exit(0);
    }
//    END CODE FOR MINIMIZE BUTTON

//    START CODE FOR MINIMIZE BUTTON
    @FXML
    public void minimizeBtnOnAction() {
        Stage stage = (Stage) main_form.getScene().getWindow();
        stage.setIconified(true);
    }
//    END CODE FOR MINIMIZE BUTTON

//    START CODE FOR USERNAME DISPLAY
    public void displayUsername () {
        username.setText(GetData.username);
    }
//    END CODE FOR USERNAME DISPLAY

//    START CODE FOR DEFAULT NAVIGATION
    public void defaultNav () {
        home_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
    }
//    END CODE FOR DEFAULT NAVIGATION

//    START CODE FOR FORM SWITCHING
    public void switchFormOnAction(ActionEvent event){
        if (event.getSource() == home_btn) {
            home_form.setVisible(true);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");
        } else if (event.getSource() == addStudent_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(true);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");
            //  TO DISPLAY DATA FROM STUDENT TABLE WHEN ADD STUDENT BUTTON IS CLICKED
            addStudentShowListData();
            addStudent_yearList();
            addStudent_genderList();
            addStudent_statusList();
            setAddStudent_courseList();
            addStudent_search_onKeyTyped();
        } else if (event.getSource() == availableCourses_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(true);
            studentAttendance_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");

            availableCourseShowListData();
        } else if (event.getSource() == studentAttendance_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(true);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
        }
    }
//    END CODE FOR FORM SWITCHING

//    START CODE FOR LOGOUT BUTTON
    @FXML
    public void logoutBtnOnAction () {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Message");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to logout?");
            Optional<javafx.scene.control.ButtonType> option = alert.showAndWait();
            if (option.get().equals(ButtonType.OK)) {
                logout_btn.getScene().getWindow().hide();
                Parent root = FXMLLoader.load(getClass().getResource("fxml/login-view.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(root);
                stage.setTitle("Login | Student Management System");
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
            }else return;

        }catch (Exception e) {e.printStackTrace();}
    }
//    END CODE FOR LOGOUT BUTTON

//    START CODE FOR ADD STUDENT FORM
    public ObservableList<StudentData> addStudentListData () {
        ObservableList<StudentData> listStudents = FXCollections.observableArrayList();
        String sql = "SELECT * FROM student";
        connect = DatabaseConnection.connectDb();
        try {
            StudentData studentD;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                studentD = new StudentData(result.getInt("studentNum"),
                        result.getString("year"),
                        result.getString("course"),
                        result.getString("firstName"),
                        result.getString("lastName"),
                        result.getString("gender"),
                        result.getDate("birthDate"),
                        result.getString("status"),
                        result.getString("image"));
                listStudents.add(studentD);
            }
        }catch (Exception e) {e.printStackTrace();}
        return listStudents;
    }
    private ObservableList <StudentData> addStudentListD;
    public void addStudentShowListData () {
        addStudentListD = addStudentListData();

        addStudent_col_studentNum.setCellValueFactory(new PropertyValueFactory<>("studentNum"));
        addStudent_col_year.setCellValueFactory(new PropertyValueFactory<>("year"));
        addStudent_col_course.setCellValueFactory(new PropertyValueFactory<>("course"));
        addStudent_col_firstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        addStudent_col_lastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        addStudent_col_gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        addStudent_col_birthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
        addStudent_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        addStudent_tableView.setItems(addStudentListD);
    }
    @FXML
    public void addStudentSelect() {
        StudentData studentD = addStudent_tableView.getSelectionModel().getSelectedItem();

        if (studentD == null) {
            return; // No selected item, exit early
        }

        addStudent_studentNum.setText(String.valueOf(studentD.getStudentNum()));
        addStudent_firstName.setText(studentD.getFirstName());
        addStudent_lastName.setText(studentD.getLastName());
        addStudent_birthDate.setValue(studentD.getBirthDate().toLocalDate());

        String uri = "file:" + studentD.getImage();
        image = new Image(uri, 120, 170, false, true);
        addStudent_imageView.setImage(image);
        GetData.path = studentD.getImage();
    }
    @FXML
    public void addStudent_yearList() {
        List<String> yearL = new ArrayList<>();
        for (String data: yearList) {
            yearL.add(data);
        }
        ObservableList ObList = FXCollections.observableArrayList(yearL);
        addStudent_year.setItems(ObList);
    }
    @FXML
    public void addStudent_genderList(){
        List <String> genderL = new ArrayList<>();
        for (String data: genderList) {
            genderL.add(data);
        }
        ObservableList ObList = FXCollections.observableArrayList(genderL);
        addStudent_gender.setItems(ObList);
    }
    @FXML
    public void addStudent_statusList(){
        List <String> statusL = new ArrayList<>();
        for (String data: statusList) {
            statusL.add(data);
        }
        ObservableList ObList = FXCollections.observableArrayList(statusL);
        addStudent_status.setItems(ObList);
    }
    @FXML
    public void setAddStudent_courseList () {
        String listCourse = "SELECT * FROM course";
        connect = DatabaseConnection.connectDb();
        try {
            ObservableList listC = FXCollections.observableArrayList();
            prepare = connect.prepareStatement(listCourse);
            result = prepare.executeQuery();
            while (result.next()){
                listC.add(result.getString("course"));
            }
            addStudent_course.setItems(listC);
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_insertBtn_onAction(){
        FileChooser open = new FileChooser();
        open.setTitle("Select Image File");
        open.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*jpg", "*png"));
        File file = open.showOpenDialog(main_form.getScene().getWindow());
        if (file != null) {
            image = new Image(file.toURI().toString(), 120, 170, false, true);
            addStudent_imageView.setImage(image);
            GetData.path = file.getAbsolutePath();
        }
    }
    @FXML
    public void addStudent_addBtn_onAction(){
        String insertData = "INSERT INTO student "
                + "(studentNum, year, course, firstName, lastName, gender, birthDate, status, image, date) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (addStudent_studentNum.getText().isEmpty()
                    || addStudent_year.getSelectionModel().getSelectedItem() == null
                    || addStudent_course.getSelectionModel().getSelectedItem() == null
                    || addStudent_firstName.getText().isEmpty()
                    || addStudent_lastName.getText().isEmpty()
                    || addStudent_gender.getSelectionModel().getSelectedItem() == null
                    || addStudent_birthDate.getValue() == null
                    || addStudent_status.getSelectionModel().getSelectedItem() == null
                    || GetData.path == null || GetData.path == "") {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Fields cannot be empty");
                alert.showAndWait();
            } else {
                // CHECK IF THE STUDENT NUMBER ALREADY EXISTS
                String checkData = "SELECT studentNum FROM student WHERE studentNum = '" + addStudent_studentNum.getText() + "'";
                statement = connect.createStatement();
                result = statement.executeQuery(checkData);
                if (result.next()) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Student #" + addStudent_studentNum.getText() + " already exist!");
                    alert.showAndWait();
                } else {
                    prepare = connect.prepareStatement(insertData);
                    prepare.setString(1, addStudent_studentNum.getText());
                    prepare.setString(2, (String) addStudent_year.getSelectionModel().getSelectedItem());
                    prepare.setString(3, (String) addStudent_course.getSelectionModel().getSelectedItem());
                    prepare.setString(4, addStudent_firstName.getText());
                    prepare.setString(5, addStudent_lastName.getText());
                    prepare.setString(6, (String) addStudent_gender.getSelectionModel().getSelectedItem());
                    prepare.setString(7, String.valueOf(addStudent_birthDate.getValue()));
                    prepare.setString(8, (String) addStudent_status.getSelectionModel().getSelectedItem());
                    String uri = GetData.path;
                    uri = uri.replace("\\", "\\\\");
                    prepare.setString(9, uri);
                    Date date = new Date();
                    java.sql.Date sqlDate = new java.sql.Date(date.getTime());
                    prepare.setString(10, String.valueOf(sqlDate));
                    prepare.executeUpdate();
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Added!");
                    alert.showAndWait();
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    addStudentShowListData();
                    // TO CLEAR THE FIELDS
                    addStudent_clearBtn_onAction();
                }
            }
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_updateBtn_onAction () {
        String uri = GetData.path;
        uri = uri.replace("\\", "\\\\");
        String updateData = "UPDATE student SET "
                + "year = '" + addStudent_year.getSelectionModel().getSelectedItem()
                + "', course = '" + addStudent_course.getSelectionModel().getSelectedItem()
                + "', firstName = '" + addStudent_firstName.getText()
                + "', lastName = '" + addStudent_lastName.getText()
                + "', gender = '" + addStudent_gender.getSelectionModel().getSelectedItem()
                + "', birthDate = '" + addStudent_birthDate.getValue()
                + "', status = '" + addStudent_status.getSelectionModel().getSelectedItem()
                + "', image = '" + uri + "' WHERE studentNum = '"
                + addStudent_studentNum.getText() + "'";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (addStudent_studentNum.getText().isEmpty()
                    || addStudent_year.getSelectionModel().getSelectedItem() == null
                    || addStudent_course.getSelectionModel().getSelectedItem() == null
                    || addStudent_firstName.getText().isEmpty()
                    || addStudent_lastName.getText().isEmpty()
                    || addStudent_gender.getSelectionModel().getSelectedItem() == null
                    || addStudent_birthDate.getValue() == null
                    || addStudent_status.getSelectionModel().getSelectedItem() == null
                    || GetData.path == null || GetData.path == "") {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Fields cannot be empty");
                alert.showAndWait();
            } else {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to update Student #: " + addStudent_studentNum.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)){
                    statement = connect.createStatement();
                    statement.executeUpdate(updateData);
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Updated!");
                    alert.showAndWait();
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    addStudentShowListData();
                    // TO CLEAR THE FIELDS
                    addStudent_clearBtn_onAction();
                } else return;
            }
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_deleteBtn_onAction () {
        String deleteData = "DELETE FROM student WHERE studentNum = '" + addStudent_studentNum.getText() + "'";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (addStudent_studentNum.getText().isEmpty()
                    || addStudent_year.getSelectionModel().getSelectedItem() == null
                    || addStudent_course.getSelectionModel().getSelectedItem() == null
                    || addStudent_firstName.getText().isEmpty()
                    || addStudent_lastName.getText().isEmpty()
                    || addStudent_gender.getSelectionModel().getSelectedItem() == null
                    || addStudent_birthDate.getValue() == null
                    || addStudent_status.getSelectionModel().getSelectedItem() == null
                    || GetData.path == null || GetData.path == "") {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Fields cannot be empty");
                alert.showAndWait();
            } else {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete Student #: " + addStudent_studentNum.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)){
                    statement = connect.createStatement();
                    statement.executeUpdate(deleteData);
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Deleted!");
                    alert.showAndWait();
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    addStudentShowListData();
                    // TO CLEAR THE FIELDS
                    addStudent_clearBtn_onAction();
                } else return;
            }
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_clearBtn_onAction () {
        addStudent_studentNum.setText("");
        addStudent_year.getSelectionModel().clearSelection();
        addStudent_course.getSelectionModel().clearSelection();
        addStudent_firstName.setText("");
        addStudent_lastName.setText("");
        addStudent_gender.getSelectionModel().clearSelection();
        addStudent_birthDate.setValue(null);
        addStudent_status.getSelectionModel().clearSelection();
        addStudent_imageView.setImage(null);
        GetData.path = "";
    }
    @FXML
    public void addStudent_search_onKeyTyped() {
        // Assuming addStudentListD is a properly populated ObservableList<StudentData>
        FilteredList<StudentData> filter = new FilteredList<>(addStudentListD, e -> true);

        // Assuming addStudent_search is your TextField for searching
        addStudent_search.textProperty().addListener((Observable, oldValue, newValue) -> {
            filter.setPredicate(predicateStudentData -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKey = newValue.toLowerCase();

                // You can simplify the conditions using a stream and anyMatch
                return Stream.of(
                        predicateStudentData.getStudentNum().toString(),
                        predicateStudentData.getYear(),
                        predicateStudentData.getCourse(),
                        predicateStudentData.getFirstName(),
                        predicateStudentData.getLastName(),
                        predicateStudentData.getGender(),
                        predicateStudentData.getBirthDate().toString(),
                        predicateStudentData.getStatus()
                ).anyMatch(data -> data.toLowerCase().contains(searchKey));
            });
        });

        SortedList<StudentData> sortList = new SortedList<>(filter);

        // Assuming addStudent_tableView is your TableView
        sortList.comparatorProperty().bind(addStudent_tableView.comparatorProperty());
        addStudent_tableView.setItems(sortList);
    }
//    END CODE FOR ADD STUDENT FORM

//    START CODE FOR AVAILABLE COURSES FORM
    public ObservableList <CourseData> availableCourseListData(){
        ObservableList <CourseData> listCourses = FXCollections.observableArrayList();
        String sql = "SELECT * FROM course";
        try {
            CourseData courseD;
            connect = DatabaseConnection.connectDb();
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()){
                courseD = new CourseData(result.getString("course"),
                        result.getString("description"),
                        result.getString("degree"));
                listCourses.add(courseD);
            }
        }catch (Exception e) {e.printStackTrace();}
        return listCourses;
    }
    private ObservableList <CourseData> availableCourseList;
    public void availableCourseShowListData () {
        availableCourseList = availableCourseListData();

        availableCourse_col_course.setCellValueFactory(new PropertyValueFactory<>("course"));
        availableCourse_col_description.setCellValueFactory(new PropertyValueFactory<>("description"));
        availableCourse_col_degree.setCellValueFactory(new PropertyValueFactory<>("degree"));

        availableCourse_tableView.setItems(availableCourseList);
    }
    @FXML
    public void availableCourseSelect () {
        CourseData courseD = availableCourse_tableView.getSelectionModel().getSelectedItem();
        int num = availableCourse_tableView.getSelectionModel().getSelectedIndex();
        if ((num - 1 ) < - 1) {return;}
        availableCourse_course.setText(courseD.getCourse());
        availableCourse_description.setText(courseD.getDescription());
        availableCourse_degree.setText(courseD.getDegree());
    }
    @FXML
    public void availableCourse_addBtn_onAction () {
        String insertData = "INSERT INTO course (course, description, degree) VALUES (?, ?, ?)";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (availableCourse_course.getText().isEmpty() || availableCourse_description.getText().isEmpty() || availableCourse_degree.getText().isEmpty()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Fields cannot be empty");
                alert.showAndWait();
            } else {
                String checkData = "SELECT course FROM course WHERE course = '" + availableCourse_course.getText() + "'";
                statement = connect.createStatement();
                result = statement.executeQuery(checkData);
                if (result.next()) {
                    alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Course: " + availableCourse_course.getText() + " already exists!");
                    alert.showAndWait();
                } else {
                    prepare = connect.prepareStatement(insertData);
                    prepare.setString(1, availableCourse_course.getText());
                    prepare.setString(2, availableCourse_description.getText());
                    prepare.setString(3, availableCourse_degree.getText());
                    prepare.executeUpdate();

                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Added!");
                    alert.showAndWait();
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    availableCourseShowListData();
                    //  TO CLEAR THE TEXT FIELDS
                    availableCourse_clearBtn_onAction();
                }
            }
        }catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void availableCourse_updateBtn_onAction () {
        String updateData = "UPDATE course SET description = '"
                + availableCourse_description.getText() + "', degree = '"
                + availableCourse_degree.getText() + "' WHERE course = '"
                + availableCourse_course.getText() + "'";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (availableCourse_course.getText().isEmpty() || availableCourse_description.getText().isEmpty() || availableCourse_degree.getText().isEmpty()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Fields cannot be empty");
                alert.showAndWait();
            } else {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to update Course: " + availableCourse_course.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)){
                    statement = connect.createStatement();
                    statement.executeUpdate(updateData);
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Updated!");
                    alert.showAndWait();
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    availableCourseShowListData();
                    //  TO CLEAR THE TEXT FIELDS
                    availableCourse_clearBtn_onAction();
                } else return;
            }
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void availableCourse_clearBtn_onAction () {
        availableCourse_course.setText("");
        availableCourse_description.setText("");
        availableCourse_degree.setText("");
    }
    @FXML
    public void availableCourse_deleteBtn_onAction() {
        String deleteData = "DELETE FROM course WHERE course = '"
                + availableCourse_course.getText() +"'";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (availableCourse_course.getText().isEmpty() || availableCourse_description.getText().isEmpty() || availableCourse_degree.getText().isEmpty()) {
                alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error Message");
                alert.setHeaderText(null);
                alert.setContentText("Fields cannot be empty");
                alert.showAndWait();
            } else {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete Course: " + availableCourse_course.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)) {
                    statement = connect.createStatement();
                    statement.executeUpdate(deleteData);
                    alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Information Message");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully Deleted!");
                    alert.showAndWait();
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    availableCourseShowListData();
                    //  TO CLEAR THE TEXT FIELDS
                    availableCourse_clearBtn_onAction();
                } else return;;
            }
        } catch (Exception e) {e.printStackTrace();}
    }
//    END CODE FOR AVAILABLE COURSES FORM
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayUsername();
        defaultNav();

        //  TO SHOW IMMEDIATELY WHEN PROCEEDED TO DASHBOARD APPLICATION FORM
        addStudentShowListData();
        addStudent_yearList();
        addStudent_genderList();
        addStudent_statusList();
        setAddStudent_courseList();
        addStudent_search_onKeyTyped();

        availableCourseShowListData();
    }
}
