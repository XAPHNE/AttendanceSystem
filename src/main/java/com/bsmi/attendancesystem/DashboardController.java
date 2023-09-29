package com.bsmi.attendancesystem;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
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
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
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
    private TableColumn<StudentData, String> addStudent_col_semester;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_status;

    @FXML
    private TableColumn<StudentData, String> addStudent_col_studentNum;

    @FXML
    private ComboBox<String> addStudent_course;

    @FXML
    private TextField addStudent_firstName;

    @FXML
    private AnchorPane addStudent_form;

    @FXML
    private ComboBox<String> addStudent_gender;

    @FXML
    private ImageView addStudent_imageView;

    @FXML
    private TextField addStudent_lastName;

    @FXML
    private TextField addStudent_search;

    @FXML
    private ComboBox<String> addStudent_semester;

    @FXML
    private ComboBox<String> addStudent_status;

    @FXML
    private TextField addStudent_studentNum;

    @FXML
    private TableView<StudentData> addStudent_tableView;

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
    private Label home_absentToday;

    @FXML
    private LineChart<String, Integer> home_absentTodayChart;

    @FXML
    private Button home_btn;

    @FXML
    private AnchorPane home_form;

    @FXML
    private Label home_presentToday;

    @FXML
    private LineChart<String, Integer> home_presentTodayChart;

    @FXML
    private Label home_totalEnrolled;

    @FXML
    private BarChart<?, ?> home_totalEnrolledChart;

    @FXML
    private Button logout_btn;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button minimize_btn;

    @FXML
    private Button studentAttendance_btn;

    @FXML
    private Button studentAttendance_clearBtn;

    @FXML
    private TableColumn<StudentAttendanceData, String> studentAttendance_col_course;

    @FXML
    private TableColumn<StudentAttendanceData, String> studentAttendance_col_entryTime;

    @FXML
    private TableColumn<StudentAttendanceData, String> studentAttendance_col_exitTime;

    @FXML
    private TableColumn<StudentAttendanceData, String> studentAttendance_col_status;

    @FXML
    private TableColumn<StudentAttendanceData, String> studentAttendance_col_studentNum;

    @FXML
    private TableColumn<StudentAttendanceData, String> studentAttendance_col_semester;

    @FXML
    private ComboBox<String> studentAttendance_course;

    @FXML
    private AnchorPane studentAttendance_form;

    @FXML
    private Button studentAttendance_markEntryBtn;

    @FXML
    private Button studentAttendance_markExitBtn;

    @FXML
    private TextField studentAttendance_search;

    @FXML
    private ComboBox<String> studentAttendance_semester;

    @FXML
    private ComboBox<String> studentAttendance_studentNum;

    @FXML
    private TableView<StudentAttendanceData> studentAttendance_tableView;

    @FXML
    private Button userManagement_addBtn;

    @FXML
    private Button userManagement_btn;

    @FXML
    private Button userManagement_clearBtn;

    @FXML
    private TableColumn<UserData, Integer> userManagement_col_serialNum;

    @FXML
    private TableColumn<UserData, String> userManagement_col_userRole;

    @FXML
    private TableColumn<UserData, String> userManagement_col_username;

    @FXML
    private TextField userManagement_confirmPassword;

    @FXML
    private Button userManagement_deleteBtn;

    @FXML
    private AnchorPane userManagement_form;

    @FXML
    private TextField userManagement_id;

    @FXML
    private Label userManagement_id_label;

    @FXML
    private TextField userManagement_password;

    @FXML
    private TableView<UserData> userManagement_tableView;

    @FXML
    private Button userManagement_updateBtn;

    @FXML
    private ComboBox<String> userManagement_userRole;

    @FXML
    private TextField userManagement_username;

    @FXML
    private Label username;

    private double xOffset = 0;
    private double yOffset = 0;
    private Connection connect;
    private PreparedStatement prepare;
    private Statement statement;
    private ResultSet result;
    private Image image;
    private final String[] semesterList = {"1st Sem", "2nd Sem", "3rd Sem", "4th Sem", "5th Sem", "6th Sem", "7th Sem", "8th Sem", "Graduated"};
    private final String[] genderList = {"Male", "Female", "Other"};
    private final String[] statusList = {"Enrolled", "Graduated", "Dropped Out"};
    private final String[] userRoleList = {"Admin", "Employee"};

    public void roleChecker() {
        String userRole = GetData.userRole;
        userManagement_btn.setVisible(Objects.equals(userRole, "Admin"));
    }

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
            userManagement_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");
            userManagement_btn.setStyle("-fx-background-color: transparent");

            homeDisplayTotalEnrolledStudents();
            homeDisplayPresentToday();
            homeDisplayAbsentToday();

            homeDisplayTotalEnrolledChart();
            homeDisplayPresentTodayChart();
            homeDisplayAbsentTodayChart();
        } else if (event.getSource() == addStudent_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(true);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(false);
            userManagement_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");
            userManagement_btn.setStyle("-fx-background-color: transparent");
            //  TO DISPLAY DATA FROM STUDENT TABLE WHEN ADD STUDENT BUTTON IS CLICKED
            addStudentShowListData();
            addStudent_semesterList();
            addStudent_genderList();
            addStudent_statusList();
            setAddStudent_courseList();
            addStudent_search_onKeyTyped();
        } else if (event.getSource() == availableCourses_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(true);
            studentAttendance_form.setVisible(false);
            userManagement_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");
            userManagement_btn.setStyle("-fx-background-color: transparent");

            availableCourseShowListData();
        } else if (event.getSource() == studentAttendance_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(true);
            userManagement_form.setVisible(false);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");
            userManagement_btn.setStyle("-fx-background-color: transparent");

            studentAttendanceShowListData();
            studentAttendanceSemesterList();
            studentAttendanceCourseList();
            studentAttendanceStudentNumList();
            studentAttendanceSearchOnKeyTyped();
        } else if (event.getSource() == userManagement_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(false);
            userManagement_form.setVisible(true);

            home_btn.setStyle("-fx-background-color: transparent");
            addStudent_btn.setStyle("-fx-background-color: transparent");
            availableCourses_btn.setStyle("-fx-background-color: transparent");
            studentAttendance_btn.setStyle("-fx-background-color: transparent");
            userManagement_btn.setStyle("-fx-background-color: linear-gradient(to bottom right, #3f82ae, #26bf7d);");

            userManagementShowListData();
            userManagement_userRoleList();
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

//COMBOBOX SET_PROMPT_TEXT ACTIONS ->

    // Method to initialize ComboBoxes with prompt text "Choose" for specific ComboBoxes
    @SafeVarargs
    private void initializeComboBoxes(ComboBox<String>... comboBoxesToSetPrompt) {
        for (ComboBox<String> comboBox : comboBoxesToSetPrompt) {
            setComboBoxPromptText(comboBox);
        }
    }

    // Create a method to set the prompt text for a ComboBox to "Choose"
    private void setComboBoxPromptText(ComboBox<String> comboBox) {
        comboBox.setPromptText("Choose");
        comboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Choose");
                } else {
                    setText(item);
                }
            }
        });
    }
    
//    START CODE FOR HOME FORM
    public void homeDisplayTotalEnrolledStudents() {
        String query = "SELECT COUNT(DISTINCT studentNum) AS enrolledCount FROM student WHERE status = 'Enrolled'";
        connect = DatabaseConnection.connectDb();
        try {
            int countEnrolled = 0;
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();
            if (result.next()){
                countEnrolled = result.getInt("enrolledCount");
            }
            home_totalEnrolled.setText(String.valueOf(countEnrolled));
            result.close();
            prepare.close();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void homeDisplayPresentToday() {
        // Get the current date as a string in the format "yyyy-MM-dd"
        String absentToday = home_totalEnrolled.getText();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = dateFormat.format(new Date());
        // Mysql query
        // String query = "SELECT COUNT(DISTINCT student_id) AS presentCount FROM student_attendance WHERE status = 'Present' AND DATE(entry_time) = ?";
        // H2 database query
        String query = "SELECT COUNT(DISTINCT student_id) AS presentCount FROM student_attendance WHERE status = 'Present' AND CAST(entry_time AS DATE) = ?";

        connect = DatabaseConnection.connectDb();
        try {
            int countPresentToday = 0;
            prepare = connect.prepareStatement(query);
            prepare.setString(1, currentDate); // Bind the current date to the query parameter
            result = prepare.executeQuery();
            if (result.next()) {
                countPresentToday = result.getInt("presentCount");
            }
            home_presentToday.setText(String.valueOf(countPresentToday));
            result.close();
            prepare.close();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void homeDisplayAbsentToday() {
        // Get the total enrolled students from the home_totalEnrolled label
        int totalEnrolled = Integer.parseInt(home_totalEnrolled.getText());

        // Get the students present today from the home_presentToday label
        int studentsPresentToday = Integer.parseInt(home_presentToday.getText());

        // Calculate the number of absent students today
        int studentsAbsentToday = totalEnrolled - studentsPresentToday;

        // Set the text of the home_absentToday label to display the number of absent students
        home_absentToday.setText(String.valueOf(studentsAbsentToday));
    }
    public void homeDisplayTotalEnrolledChart () {
        home_totalEnrolledChart.getData().clear();
        // MySQL query
        // String query = "SELECT added_on, COUNT(DISTINCT id) FROM student WHERE status = 'Enrolled' GROUP BY added_on ORDER BY TIMESTAMP(added_on) ASC LIMIT 5";
        // H2 database query
        String query = "SELECT added_on, COUNT(DISTINCT id) FROM student WHERE status = 'Enrolled' GROUP BY added_on ORDER BY CAST(added_on AS TIMESTAMP) ASC LIMIT 5";
        connect = DatabaseConnection.connectDb();
        try {
            XYChart.Series chart = new XYChart.Series();
            prepare = connect.prepareStatement(query);
            result = prepare.executeQuery();
            while (result.next()){
                chart.getData().add(new XYChart.Data(result.getString(1), result.getInt(2)));
            }
            home_totalEnrolledChart.getData().add(chart);
            result.close();
            prepare.close();
            connect.close();
        } catch (Exception e) {e.printStackTrace();}
    }
    public void homeDisplayPresentTodayChart() {
        // Clear any existing data from the chart
        home_presentTodayChart.getData().clear();

        // Define the SQL query to retrieve data for the chart
        // MySQL query
        // String sql = "SELECT DATE(entry_time) AS date, COUNT(DISTINCT id) FROM student_attendance WHERE DATE(entry_time) = CURDATE() GROUP BY DATE(entry_time)";
        // H2 database query
        String sql = "SELECT CAST(entry_time AS DATE) AS date, COUNT(DISTINCT id) FROM student_attendance WHERE CAST(entry_time AS DATE) = CURRENT_DATE GROUP BY CAST(entry_time AS DATE)";

        // Establish a database connection (replace with your connection code)
        connect = DatabaseConnection.connectDb();

        try {
            // Create a new XYChart.Series for the line chart
            XYChart.Series<String, Integer> series = new XYChart.Series<>();

            // Prepare the SQL statement
            prepare = connect.prepareStatement(sql);

            // Execute the query and retrieve the result set
            result = prepare.executeQuery();

            // Iterate through the result set and add data to the series
            while (result.next()) {
                String date = result.getString("date");
                int count = result.getInt(2);
                series.getData().add(new XYChart.Data<>(date, count));
            }

            // Add the series to the line chart
            home_presentTodayChart.getData().add(series);
            result.close();
            prepare.close();
            connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void homeDisplayAbsentTodayChart() {
        // Clear any existing data from the chart
        home_absentTodayChart.getData().clear();

        // Get the total enrolled students from the home_totalEnrolled label
        int totalEnrolled = Integer.parseInt(home_totalEnrolled.getText());

        // Get the students present today from the home_presentTodayChart line chart
        LineChart.Series<String, Integer> presentSeries = (LineChart.Series<String, Integer>) home_presentTodayChart.getData().get(0);

        // Create a new XYChart.Series for the absent students
        XYChart.Series<String, Integer> absentSeries = new XYChart.Series<>();

        // Iterate through the data in the presentSeries and calculate absent students
        for (XYChart.Data<String, Integer> data : presentSeries.getData()) {
            String date = data.getXValue();
            int presentCount = data.getYValue();
            int absentCount = totalEnrolled - presentCount;
            absentSeries.getData().add(new XYChart.Data<>(date, absentCount));
        }

        // Add the absentSeries to the home_absentTodayChart line chart
        home_absentTodayChart.getData().add(absentSeries);
    }
//    END CODE FOR HOME FORM

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
                        result.getString("semester"),
                        result.getString("course"),
                        result.getString("firstName"),
                        result.getString("lastName"),
                        result.getString("gender"),
                        result.getDate("birthDate"),
                        result.getString("status"),
                        result.getString("image"));
                listStudents.add(studentD);
            }
            result.close();
            prepare.close();
            connect.close();
        }catch (Exception e) {e.printStackTrace();}
        return listStudents;
    }
    private ObservableList <StudentData> addStudentListD;
    public void addStudentShowListData () {
        addStudentListD = addStudentListData();

        addStudent_col_studentNum.setCellValueFactory(new PropertyValueFactory<>("studentNum"));
        addStudent_col_semester.setCellValueFactory(new PropertyValueFactory<>("semester"));
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
        addStudent_semester.getSelectionModel().select(studentD.getSemester());
        addStudent_course.getSelectionModel().select(studentD.getCourse());
        addStudent_firstName.setText(studentD.getFirstName());
        addStudent_lastName.setText(studentD.getLastName());
        addStudent_gender.getSelectionModel().select(studentD.getGender());
        addStudent_birthDate.setValue(studentD.getBirthDate().toLocalDate());
        addStudent_status.getSelectionModel().select(studentD.getStatus());


        String uri = "file:" + studentD.getImage();
        image = new Image(uri, 120, 170, false, true);
        addStudent_imageView.setImage(image);
        GetData.path = studentD.getImage();
    }
    @FXML
    public void addStudent_semesterList() {
        List<String> semesterL = new ArrayList<>();
        for (String data: semesterList) {
            semesterL.add(data);
        }
        ObservableList ObList = FXCollections.observableArrayList(semesterL);
        addStudent_semester.setItems(ObList);
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
            result.close();
            prepare.close();
            connect.close();
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
                + "(studentNum, semester, course, firstName, lastName, gender, birthDate, status, image, added_on) "
                + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (addStudent_studentNum.getText().isEmpty()
                    || addStudent_semester.getSelectionModel().getSelectedItem() == null
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
                    prepare.setString(2, (String) addStudent_semester.getSelectionModel().getSelectedItem());
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
                prepare.close();
                result.close();
                statement.close();
            }
            connect.close();
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_updateBtn_onAction () {
        String uri = GetData.path;
        uri = uri.replace("\\", "\\\\");
        String updateData = "UPDATE student SET "
                + "semester = '" + addStudent_semester.getSelectionModel().getSelectedItem()
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
                    || addStudent_semester.getSelectionModel().getSelectedItem() == null
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
                statement.close();
            }
            connect.close();
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_deleteBtn_onAction () {
        String deleteData = "DELETE FROM student WHERE studentNum = '" + addStudent_studentNum.getText() + "'";
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (addStudent_studentNum.getText().isEmpty()
                    || addStudent_semester.getSelectionModel().getSelectedItem() == null
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
                statement.close();
            }
            connect.close();
        } catch (Exception e) {e.printStackTrace();}
    }
    @FXML
    public void addStudent_clearBtn_onAction () {
        addStudent_studentNum.setText("");
        addStudent_semester.getSelectionModel().clearSelection();
        addStudent_course.getSelectionModel().clearSelection();
        addStudent_firstName.setText("");
        addStudent_lastName.setText("");
        addStudent_gender.getSelectionModel().clearSelection();
        addStudent_birthDate.setValue(null);
        addStudent_status.getSelectionModel().clearSelection();
        addStudent_imageView.setImage(null);
        GetData.path = "";

        initializeComboBoxes(addStudent_semester, addStudent_course, addStudent_gender, addStudent_status);
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
                        predicateStudentData.getSemester(),
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
            result.close();
            prepare.close();
            connect.close();
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
                prepare.close();
                result.close();
                statement.close();
            }
            connect.close();
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
                statement.close();
            }
            connect.close();
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
                } else return;
                statement.close();
            }
            connect.close();
        } catch (Exception e) {e.printStackTrace();}
    }
//    END CODE FOR AVAILABLE COURSES FORM

//    START CODE FOR STUDENT ATTENDANCE FORM
    public ObservableList <StudentAttendanceData> studentAttendanceListData(){
        ObservableList <StudentAttendanceData> listStudentAttendance = FXCollections.observableArrayList();
        // MySQL query
        /* String query = "SELECT student.studentNum, student.semester, student.course, " +
                "student_attendance.entry_time, student_attendance.exit_time, student_attendance.status " +
                "FROM student_attendance " +
                "INNER JOIN student ON student_attendance.student_id = student.id " +
                "INNER JOIN course ON student_attendance.course_id = course.id " +
                "WHERE DATE(student_attendance.entry_time) = CURDATE()"; */
        // H2 database query
        String query = "SELECT student.studentNum, student.semester, student.course, " +
                "       student_attendance.entry_time, student_attendance.exit_time, student_attendance.status " +
                "FROM student_attendance " +
                "INNER JOIN student ON student_attendance.student_id = student.id " +
                "INNER JOIN course ON student_attendance.course_id = course.id " +
                "WHERE CAST(student_attendance.entry_time AS DATE) = CURRENT_DATE()";
        connect = DatabaseConnection.connectDb();
        if (connect != null) {
            try {
                StudentAttendanceData record;
                prepare = connect.prepareStatement(query);
                result = prepare.executeQuery();
                while (result.next()) {
                    record = new StudentAttendanceData(result.getString("studentNum"),
                            result.getString("semester"),
                            result.getString("course"),
                            result.getString("entry_time"),
                            result.getString("exit_time"),
                            result.getString("status"));
                    listStudentAttendance.add(record);
                }
                result.close();
                prepare.close();
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return listStudentAttendance;
    }
    private ObservableList<StudentAttendanceData> observableAttendanceData;
    @FXML
    public void studentAttendanceShowListData(){
        observableAttendanceData = studentAttendanceListData();
        studentAttendance_col_studentNum.setCellValueFactory(new PropertyValueFactory<>("studentNum"));
        studentAttendance_col_semester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        studentAttendance_col_course.setCellValueFactory(new PropertyValueFactory<>("course"));
        studentAttendance_col_entryTime.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        studentAttendance_col_exitTime.setCellValueFactory(new PropertyValueFactory<>("exitTime"));
        studentAttendance_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        studentAttendance_tableView.setItems(observableAttendanceData);
    }
    @FXML
    public void studentAttendanceSemesterList(){
        //  studentAttendance_semester.getItems().clear();
        List <String> semesterL = new ArrayList<>();
        for (String data: semesterList){
            semesterL.add(data);
        }
        ObservableList <String> ObList = FXCollections.observableArrayList(semesterL);
        studentAttendance_semester.setItems(ObList);

        studentAttendance_semester.setOnAction(e -> studentAttendanceCourseList());
    }
    @FXML
    public void studentAttendanceCourseList(){
        //  studentAttendance_course.getItems().clear();
        String selectedSemester = studentAttendance_semester.getSelectionModel().getSelectedItem();
        connect = DatabaseConnection.connectDb();
        if (connect != null){
            try {
                String query = "SELECT DISTINCT course FROM student WHERE semester = ?";
                prepare = connect.prepareStatement(query);
                prepare.setString(1, selectedSemester);
                result = prepare.executeQuery();
                ObservableList <String> courseList = FXCollections.observableArrayList();
                while (result.next()){
                    String course = result.getString("course");
                    courseList.add(course);
                }
                studentAttendance_course.setItems(courseList);
                result.close();
                prepare.close();
                connect.close();
            } catch (SQLException e) {e.printStackTrace();}
        }
        
        studentAttendance_course.setOnAction(e -> studentAttendanceStudentNumList());
    }
    @FXML
    public void studentAttendanceStudentNumList(){
        String selectedSemester = studentAttendance_semester.getSelectionModel().getSelectedItem();
        String selectedCourse = studentAttendance_course.getSelectionModel().getSelectedItem();
        /*
          studentAttendance_studentNum.getItems().clear();
        */
        connect = DatabaseConnection.connectDb();
        if (connect != null) {
            try {

                String query = "SELECT DISTINCT studentNum FROM student WHERE semester = ? AND course = ?";
                prepare = connect.prepareStatement(query);
                prepare.setString(1, selectedSemester);
                prepare.setString(2, selectedCourse);
                
                result = prepare.executeQuery();
                ObservableList <String> studentNumList = FXCollections.observableArrayList();
                while (result.next()) {
                    String studentNum = result.getString("studentNum");
                    studentNumList.add(studentNum);
                }
                studentAttendance_studentNum.setItems(studentNumList);
                result.close();
                prepare.close();
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    // Method to fetch student_id based on studentNum, semester, and course
    private int fetchStudentId(String studentNum, String semester, String course) {
        int studentId = -1; // Initialize to a default value (e.g., -1) to indicate no matching student found

        connect = DatabaseConnection.connectDb();

        if (connect != null) {
            try {
                String query = "SELECT id FROM student WHERE studentNum = ? AND semester = ? AND course = ?";
                PreparedStatement preparedStatement = connect.prepareStatement(query);
                preparedStatement.setString(1, studentNum);
                preparedStatement.setString(2, semester);
                preparedStatement.setString(3, course);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    studentId = resultSet.getInt("id");
                }

                // Close resources
                resultSet.close();
                preparedStatement.close();
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle any database-related exceptions here
            }
        }

        return studentId;
    }
    // Method to fetch course_id based on course
    private int fetchCourseId(String course) {
        int courseId = -1; // Initialize to a default value (e.g., -1) to indicate no matching course found

        connect = DatabaseConnection.connectDb();

        if (connect != null) {
            try {
                String query = "SELECT id FROM course WHERE course = ?";
                PreparedStatement preparedStatement = connect.prepareStatement(query);
                preparedStatement.setString(1, course);

                ResultSet resultSet = preparedStatement.executeQuery();

                if (resultSet.next()) {
                    courseId = resultSet.getInt("id");
                }

                // Close resources
                resultSet.close();
                preparedStatement.close();
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle any database-related exceptions here
            }
        }

        return courseId;
    }
    @FXML
    public void studentAttendanceMarkEntryBtnOnAction() {
        String selectedStudentNum = studentAttendance_studentNum.getValue();
        String selectedSemester = studentAttendance_semester.getValue();
        String selectedCourse = studentAttendance_course.getValue();

        // Check if all required fields are selected
        if (selectedStudentNum != null && selectedSemester != null && selectedCourse != null) {
            // Get the current timestamp
            Timestamp entryTime = new Timestamp(new Date().getTime());

            // Database connection and SQL query to insert data
            connect = DatabaseConnection.connectDb(); // Replace with your actual database connection code

            if (connect != null) {
                try {
                    String query = "INSERT INTO student_attendance (student_id, course_id, entry_time, exit_time, status) VALUES (?, ?, ?, NULL, 'Present')";
                    prepare = connect.prepareStatement(query);

                    // You need to fetch the student_id and course_id based on the selected studentNum, semester, and course
                    int studentId = fetchStudentId(selectedStudentNum, selectedSemester, selectedCourse);
                    int courseId = fetchCourseId(selectedCourse);

                    prepare.setInt(1, studentId);
                    prepare.setInt(2, courseId);
                    prepare.setTimestamp(3, entryTime);

                    // Execute the insert query
                    prepare.executeUpdate();

                    // Close the database connection and resources
                    prepare.close();
                    connect.close();

                    // Show a success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Information Message");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Attendance marked successfully!");
                    successAlert.showAndWait();

                    // Optionally, you can perform any other actions here
                    studentAttendanceShowListData();
                    studentAttendanceClearBtnOnAction();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // Handle any database-related exceptions here

                    // Display a database error message
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error Message");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("An error occurred while marking attendance. Please try again later.");
                    errorAlert.showAndWait();
                }
            }
        } else {
            // Handle the case where not all required fields are selected
            // Display an error message or take appropriate action
            Alert missingFieldsAlert = new Alert(Alert.AlertType.ERROR);
            missingFieldsAlert.setTitle("Error Message");
            missingFieldsAlert.setHeaderText(null);
            missingFieldsAlert.setContentText("Please select all required fields.");
            missingFieldsAlert.showAndWait();
        }
    }
    @FXML
    public void studentAttendanceMarkExitOnAction() {
        String selectedSemester = studentAttendance_semester.getSelectionModel().getSelectedItem();
        String selectedCourse = studentAttendance_course.getSelectionModel().getSelectedItem();
        String selectedStudentNum = studentAttendance_studentNum.getSelectionModel().getSelectedItem();
        connect = DatabaseConnection.connectDb(); // Replace with your actual database connection code

        if (connect != null) {
            try {
                // Get the current date and time as a Timestamp
                Timestamp currentTimestamp = new Timestamp(new Date().getTime());

                // Define a SQL query to update the exit_time
                String query = "UPDATE student_attendance SET exit_time = ? " +
                        "WHERE entry_time >= ? AND entry_time < ? " +
                        "AND exit_time IS NULL " +
                        "AND student_id IN (SELECT id FROM student WHERE semester = ? AND course = ? AND studentNum = ?)";

                // Set a time range for checking entry_time (e.g., for the current day)
                // You may need to adjust this time range based on your specific requirements
                Timestamp startOfDay = new Timestamp(new Date().getTime());
                startOfDay.setHours(0);
                startOfDay.setMinutes(0);
                startOfDay.setSeconds(0);

                Timestamp endOfDay = new Timestamp(new Date().getTime());
                endOfDay.setHours(23);
                endOfDay.setMinutes(59);
                endOfDay.setSeconds(59);

                // Create a PreparedStatement for the query
                prepare = connect.prepareStatement(query);
                prepare.setTimestamp(1, currentTimestamp);
                prepare.setTimestamp(2, startOfDay);
                prepare.setTimestamp(3, endOfDay);
                prepare.setString(4, selectedSemester);
                prepare.setString(5, selectedCourse);
                prepare.setString(6, selectedStudentNum);

                // Execute the update query
                int updatedRows = prepare.executeUpdate();

                // Close the PreparedStatement and database connection
                prepare.close();
                connect.close();

                if (updatedRows > 0) {
                    // Show a success message
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Information Message");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Exit times updated for " + updatedRows + " records.");
                    successAlert.showAndWait();

                    // Optionally, you can perform any other actions here
                    studentAttendanceShowListData();
                    studentAttendanceClearBtnOnAction();
                } else {
                    // Show a message when no records were updated
                    Alert noRecordsAlert = new Alert(Alert.AlertType.INFORMATION);
                    noRecordsAlert.setTitle("Information Message");
                    noRecordsAlert.setHeaderText(null);
                    noRecordsAlert.setContentText("No records found for update.");
                    noRecordsAlert.showAndWait();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle any database-related exceptions here

                // Display a database error message
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Error Message");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("An error occurred while updating exit times. Please try again later.");
                errorAlert.showAndWait();
            }
        } else {
            // Handle the case where the database connection is null
            Alert dbConnectionErrorAlert = new Alert(Alert.AlertType.ERROR);
            dbConnectionErrorAlert.setTitle("Error Message");
            dbConnectionErrorAlert.setHeaderText(null);
            dbConnectionErrorAlert.setContentText("Unable to connect to the database. Please check your database connection.");
            dbConnectionErrorAlert.showAndWait();
        }
    }
    
    @FXML
    public void studentAttendanceClearBtnOnAction() {
        studentAttendance_semester.getSelectionModel().clearSelection();
        studentAttendance_course.getSelectionModel().clearSelection();;
        studentAttendance_studentNum.getSelectionModel().clearSelection();

        initializeComboBoxes(studentAttendance_semester, studentAttendance_course, studentAttendance_studentNum);
    }
    
    @FXML
    public void studentAttendanceSearchOnKeyTyped() {
        // Assuming observableAttendanceData is a properly populated ObservableList<StudentAttendanceData>
        FilteredList<StudentAttendanceData> filter = new FilteredList<>(observableAttendanceData, e -> true);

        // Assuming addStudent_search is your TextField for searching
        studentAttendance_search.textProperty().addListener((Observable, oldValue, newValue) -> {
            filter.setPredicate(predicateStudentData -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKey = newValue.toLowerCase();

                // You can simplify the conditions using a stream and anyMatch
                return Stream.of(
                        predicateStudentData.getStudentNum().toString(),
                        predicateStudentData.getSemester(),
                        predicateStudentData.getCourse(),
                        predicateStudentData.getEntryTime(),
                        //predicateStudentData.getExitTime(),
                        predicateStudentData.getStatus()
                ).anyMatch(data -> data.toLowerCase().contains(searchKey));
            });
        });

        SortedList<StudentAttendanceData> sortList = new SortedList<>(filter);

        // Assuming studentAttendance_tableView is your TableView
        sortList.comparatorProperty().bind(studentAttendance_tableView.comparatorProperty());
        studentAttendance_tableView.setItems(sortList);
    }
//    END CODE FOR STUDENT ATTENDANCE FORM

//    START MANAGE USERS FORM
    public ObservableList<UserData> userManagementListData () {
    ObservableList<UserData> listUsers = FXCollections.observableArrayList();
    String sql = "SELECT * FROM admin";
    connect = DatabaseConnection.connectDb();
    if (connect != null) {
        try {
            UserData record;
            prepare = connect.prepareStatement(sql);
            result = prepare.executeQuery();
            while (result.next()) {
                record = new UserData(result.getInt("id"),
                        result.getString("username"),
                        result.getString("user_role"));
                listUsers.add(record);
            }
            result.close();
            prepare.close();
            connect.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    return listUsers;
}
    private ObservableList <UserData> observableUserManagementData;
    public void userManagementShowListData () {
        observableUserManagementData = userManagementListData();

        userManagement_col_serialNum.setCellValueFactory(cellData -> {
            int index = observableUserManagementData.indexOf(cellData.getValue()) + 1;
            return new SimpleIntegerProperty(index).asObject();
        });
        userManagement_col_username.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userManagement_col_userRole.setCellValueFactory(new PropertyValueFactory<>("userRole"));

        userManagement_tableView.setItems(observableUserManagementData);
    }
    @FXML
    public void userManagementSelect() {
        // Get the selected item from the TableView
        UserData selectedUser = userManagement_tableView.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            // Set the selected data to the input fields
            userManagement_id.setText(String.valueOf(selectedUser.getUserId()));
            userManagement_username.setText(selectedUser.getUserName());
            userManagement_userRole.getSelectionModel().select(selectedUser.getUserRole());
        }
    }

    @FXML
    public void userManagement_userRoleList() {
        List<String> userRoleL = new ArrayList<>();
        Collections.addAll(userRoleL, userRoleList);
        ObservableList<String> ObList = FXCollections.observableArrayList(userRoleL);
        userManagement_userRole.setItems(ObList);
    }
    @FXML
    public void userManagementAddBtn_onAction() {
        String username = userManagement_username.getText();
        String password = userManagement_password.getText();
        String confirmPassword = userManagement_confirmPassword.getText();
        String userRole = userManagement_userRole.getValue();

        if (Objects.equals(username, "") || Objects.equals(password, "") || Objects.equals(confirmPassword, "") || userRole == null) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Fields cannot be empty");
            return; // Exit the method if empty
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Passwords do not match!");
            return; // Exit the method if passwords don't match
        }
        String hashedPassword = hashPassword(password);

        // Insert the data into the database
        String query = "INSERT INTO admin (username, password_hash, user_role) VALUES (?, ?, ?)";
        connect = DatabaseConnection.connectDb();

        try {
            // Check if the user already exists
            String checkData = "SELECT username FROM admin WHERE username = ?";
            assert connect != null;
            PreparedStatement checkStatement = connect.prepareStatement(checkData);
            checkStatement.setString(1, username);
            ResultSet result = checkStatement.executeQuery();
            if (result.next()) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "User '" + username + "' already exists!");
                return; // Exit if user already exists
            }
            checkStatement.close();

            // Insert the new user
            prepare = connect.prepareStatement(query);
            prepare.setString(1, username);
            prepare.setString(2, hashedPassword);
            prepare.setString(3, userRole);
            prepare.executeUpdate();

            showAlert(Alert.AlertType.INFORMATION, "Information Message", "User added successfully!");

            userManagementShowListData();
            // Clear the fields if needed
            userManagementClearBtn_onAction();

            prepare.close();
            connect.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database-related exceptions
        }
    }
    private int fetchUserId(String username) {
        int userId = -1;
        connect = DatabaseConnection.connectDb();
        if (connect != null) {
            try {
                String query = "SELECT id FROM admin WHERE username = ?";
                PreparedStatement preparedStatement = connect.prepareStatement(query);
                preparedStatement.setString(1, username);
                ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    userId = resultSet.getInt("id");
                }
            } catch (SQLException e) {e.printStackTrace();}
        }
        return userId;
    }
    private boolean usernameExists(String username) {
        String query = "SELECT COUNT(*) FROM admin WHERE username = ?";
        try {
            PreparedStatement preparedStatement = connect.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    @FXML
    public void userManagementUpdateBtn_onAction() {
        String userIdText = userManagement_id.getText();
        if (userIdText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Fields cannot be empty");
            return; 
        }

        int userId = Integer.parseInt(userIdText);
        //int userId = Integer.parseInt(userManagement_id.getText());
        String username = userManagement_username.getText();
        String password = userManagement_password.getText();
        String confirmPassword = userManagement_confirmPassword.getText();
        String userRole = userManagement_userRole.getValue();
        String hashedPassword = hashPassword(password);

        // Check if fields are empty
        if (password.isEmpty() || confirmPassword.isEmpty() || userRole == null) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Fields cannot be empty");
            return;
        }

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Error Message", "Passwords do not match!");
            return; // Exit the method if passwords don't match
        }

        connect = DatabaseConnection.connectDb();
        if (connect != null) {
            try {
                if (userId != fetchUserId(username) && usernameExists(username)) {
                    showAlert(Alert.AlertType.ERROR, "Error Message", "User " + username + " already exists");
                    return;
                }
                String query = "UPDATE admin SET " +
                        "username = ?, password_hash = ?, user_role = ? " +
                        "WHERE id = ?";
                prepare = connect.prepareStatement(query);
                prepare.setString(1, username);
                prepare.setString(2, hashedPassword);
                prepare.setString(3, userRole);
                prepare.setInt(4, userId);

                Alert alert;
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to update?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)) {
                    int updatedRows = prepare.executeUpdate();
                    if (updatedRows > 0) {
                        showAlert(Alert.AlertType.INFORMATION, "Information Message", "Updated Successfully");

                        userManagementShowListData();
                        // Clear the fields if needed
                        userManagementClearBtn_onAction();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error Message", "No rows were updated.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (prepare != null) {
                        prepare.close();
                    }
                    if (connect != null) {
                        connect.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @FXML
    public void userManagementDeleteBtn_onAction() {
        String deleteData = "DELETE FROM admin WHERE id = " + userManagement_id.getText();
        connect = DatabaseConnection.connectDb();
        try {
            Alert alert;
            if (userManagement_id.getText().isEmpty() || userManagement_username.getText().isEmpty() || userManagement_userRole.getSelectionModel().getSelectedItem().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Error Message", "Fields cannot be empty");
            } else if (Integer.parseInt(userManagement_id.getText()) == fetchUserId(userManagement_username.getText())){
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation Message");
                alert.setHeaderText(null);
                alert.setContentText("Are you sure you want to delete User: " + userManagement_username.getText() + "?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.get().equals(ButtonType.OK)) {
                    statement = connect.createStatement();
                    statement.executeUpdate(deleteData);
                    showAlert(Alert.AlertType.INFORMATION, "Information Message", "Successfully Deleted!");
                    //  TO LOAD THE UPDATED TABLE AFTER OPERATION
                    userManagementShowListData();
                    // Clear the fields if needed
                    userManagementClearBtn_onAction();
                } else return;
                statement.close();
            }
            connect.close();
        } catch (Exception e) {e.printStackTrace();}
    }
    private void showAlert(Alert.AlertType alertType, String title, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
    }

    // Implementing a password hashing method here
    private String hashPassword(String password) {
        // Generate a salt for hashing (you can configure the strength as needed)
        String salt = BCrypt.gensalt(12);
        // Hash the password with the salt
        return BCrypt.hashpw(password, salt);
    }

    @FXML
    void userManagementClearBtn_onAction() {
        // Clear the input fields here
        userManagement_id.clear();
        userManagement_username.clear();
        userManagement_password.clear();
        userManagement_confirmPassword.clear();
        userManagement_userRole.getSelectionModel().clearSelection();

        initializeComboBoxes(userManagement_userRole);
    }
    public void hideUserId() {
        userManagement_id_label.setVisible(false);
        userManagement_id.setVisible(false);
    }

    //    END MANAGE USERS FORM
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleChecker();
        displayUsername();
        defaultNav();
        homeDisplayTotalEnrolledStudents();
        homeDisplayPresentToday();
        homeDisplayAbsentToday();
        homeDisplayTotalEnrolledChart();
        homeDisplayPresentTodayChart();
        homeDisplayAbsentTodayChart();

        //  TO SHOW IMMEDIATELY WHEN PROCEEDED TO DASHBOARD APPLICATION FORM
        addStudentShowListData();
        addStudent_semesterList();
        addStudent_genderList();
        addStudent_statusList();
        setAddStudent_courseList();
        addStudent_search_onKeyTyped();

        availableCourseShowListData();

        //  Initialize the ComboBox when the controller is loaded
        studentAttendanceSemesterList();
        studentAttendanceCourseList();
        studentAttendanceStudentNumList();
        studentAttendanceShowListData();
        studentAttendanceSearchOnKeyTyped();

        hideUserId();
        userManagementShowListData();
        userManagement_userRoleList();
    }
}
