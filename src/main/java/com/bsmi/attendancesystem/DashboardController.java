package com.bsmi.attendancesystem;

import javafx.application.Platform;
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
import com.bsmi.attendancesystem.dao.AttendanceDao;
import com.bsmi.attendancesystem.dao.CourseDao;
import com.bsmi.attendancesystem.dao.DataAccessException;
import com.bsmi.attendancesystem.dao.Database;
import com.bsmi.attendancesystem.dao.StudentDao;
import com.bsmi.attendancesystem.dao.UserDao;
import com.bsmi.attendancesystem.service.AttendanceService;
import com.bsmi.attendancesystem.service.CourseService;
import com.bsmi.attendancesystem.service.ImageStore;
import com.bsmi.attendancesystem.service.NotSelectedException;
import com.bsmi.attendancesystem.service.StudentCards;
import com.bsmi.attendancesystem.service.StudentService;
import com.bsmi.attendancesystem.service.UserForm;
import com.bsmi.attendancesystem.service.UserService;
import com.bsmi.attendancesystem.service.ValidationException;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Stream;

public class DashboardController implements Initializable {
    private static final String ACTIVE_NAV_STYLE = "nav-btn-active";

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
    private TextField addStudent_cardId;

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
    private TextField studentAttendance_scanCard;

    @FXML
    private Label studentAttendance_scanResult;

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
    private Image image;
    /** The stored photo name of the student in the Add Student form, empty when none is chosen. */
    private String chosenPhotoName = "";
    private final String[] semesterList = {"1st Sem", "2nd Sem", "3rd Sem", "4th Sem", "5th Sem", "6th Sem", "7th Sem", "8th Sem", "Graduated"};
    private final String[] genderList = {"Male", "Female", "Other"};
    private final String[] statusList = {"Enrolled", "Graduated", "Dropped Out"};

    public void roleChecker() {
        userManagement_btn.setVisible(UserSession.isAdmin());
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
        username.setText(UserSession.current().getUserName());
    }
//    END CODE FOR USERNAME DISPLAY

//    START CODE FOR DEFAULT NAVIGATION
    public void defaultNav () {
        highlightNavButton(home_btn);
    }

    private void highlightNavButton(Button openScreenButton) {
        List.of(home_btn, addStudent_btn, availableCourses_btn, studentAttendance_btn, userManagement_btn)
                .forEach(navButton -> navButton.getStyleClass().remove(ACTIVE_NAV_STYLE));
        openScreenButton.getStyleClass().add(ACTIVE_NAV_STYLE);
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

            highlightNavButton(home_btn);

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

            highlightNavButton(addStudent_btn);
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

            highlightNavButton(availableCourses_btn);

            availableCourseShowListData();
        } else if (event.getSource() == studentAttendance_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(true);
            userManagement_form.setVisible(false);

            highlightNavButton(studentAttendance_btn);

            studentAttendanceShowListData();
            studentAttendanceSemesterList();
            studentAttendanceCourseList();
            studentAttendanceStudentNumList();
            studentAttendanceSearchOnKeyTyped();
            // A card reader types into whatever has focus, so the scan field gets it
            Platform.runLater(studentAttendance_scanCard::requestFocus);
        } else if (event.getSource() == userManagement_btn) {
            home_form.setVisible(false);
            addStudent_form.setVisible(false);
            availableCourse_form.setVisible(false);
            studentAttendance_form.setVisible(false);
            userManagement_form.setVisible(true);

            highlightNavButton(userManagement_btn);

            userManagementShowListData();
            userManagement_userRoleList();
        }
    }
//    END CODE FOR FORM SWITCHING

//    START CODE FOR LOGOUT BUTTON
    @FXML
    public void logoutBtnOnAction () {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Message");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        if (alert.showAndWait().filter(ButtonType.OK::equals).isEmpty()) {
            return;
        }
        UserSession.end();
        logout_btn.getScene().getWindow().hide();
        try {
            openLoginScreen();
        } catch (IOException e) {
            // A broken FXML file is a packaging bug, not something the user can fix
            throw new UncheckedIOException("Could not load the login screen", e);
        }
    }

    private void openLoginScreen() throws IOException {
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
    private static final int CHART_DAYS = 5;

    private final AttendanceService attendanceService =
            new AttendanceService(
                    new AttendanceDao(Database.fromCurrentSettings()),
                    new StudentDao(Database.fromCurrentSettings()));

    public void homeDisplayTotalEnrolledStudents() {
        showTodaySummary(summary -> home_totalEnrolled.setText(String.valueOf(summary.enrolled())));
    }
    public void homeDisplayPresentToday() {
        showTodaySummary(summary -> home_presentToday.setText(String.valueOf(summary.present())));
    }
    public void homeDisplayAbsentToday() {
        showTodaySummary(summary -> home_absentToday.setText(String.valueOf(summary.absent())));
    }
    private void showTodaySummary(java.util.function.Consumer<AttendanceService.DaySummary> show) {
        try {
            show.accept(attendanceService.summarizeToday());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    public void homeDisplayTotalEnrolledChart () {
        showChart(home_totalEnrolledChart, () -> attendanceService.countEnrolledAddedPerDay(CHART_DAYS));
    }
    public void homeDisplayPresentTodayChart() {
        showChart(home_presentTodayChart, () -> attendanceService.countPresentPerDay(CHART_DAYS));
    }
    public void homeDisplayAbsentTodayChart() {
        showChart(home_absentTodayChart, () -> attendanceService.countAbsentPerDay(CHART_DAYS));
    }
    @SuppressWarnings({"rawtypes", "unchecked"}) // the FXML gives the bar chart wildcard types
    private void showChart(XYChart chart, java.util.function.Supplier<Map<LocalDate, Integer>> countPerDay) {
        chart.getData().clear();
        try {
            XYChart.Series<String, Integer> series = new XYChart.Series<>();
            countPerDay.get().forEach((day, count) -> series.getData().add(new XYChart.Data<>(day.toString(), count)));
            chart.getData().add(series);
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
//    END CODE FOR HOME FORM

//    START CODE FOR ADD STUDENT FORM
    private final StudentService studentService =
            new StudentService(new StudentDao(Database.fromCurrentSettings()));
    private final ImageStore imageStore = new ImageStore(AppPaths.dataDirectory().resolve("images"));

    public ObservableList<StudentData> addStudentListData () {
        try {
            return FXCollections.observableArrayList(studentService.listAll());
        } catch (DataAccessException e) {
            showDatabaseError(e);
            return FXCollections.observableArrayList();
        }
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
        addStudent_cardId.setText(studentD.getCardId() == null ? "" : studentD.getCardId());


        showPhoto(studentD.getImage());
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
        try {
            addStudent_course.setItems(FXCollections.observableArrayList(
                    courseService.listAll().stream().map(CourseData::getCourse).toList()));
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void addStudent_insertBtn_onAction(){
        FileChooser open = new FileChooser();
        open.setTitle("Select Image File");
        open.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image File", "*.png", "*.jpg", "*.jpeg"));
        File file = open.showOpenDialog(main_form.getScene().getWindow());
        if (file == null) {
            return;
        }
        try {
            showPhoto(imageStore.save(file.toPath()));
        } catch (ValidationException | UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        }
    }
    @FXML
    public void addStudent_saveCardBtn_onAction() {
        try {
            StudentData student = addStudent_tableView.getSelectionModel().getSelectedItem();
            if (student == null) {
                throw new NotSelectedException("student");
            }
            String name = student.getFirstName() + " " + student.getLastName();
            if (student.getCardId() == null) {
                if (!confirmed(name + " has no card yet. Give them a new card ID?")) {
                    return;
                }
                student = studentService.issueCard(student);
                addStudentShowListData();
                addStudent_cardId.setText(student.getCardId());
            }
            FileChooser save = new FileChooser();
            save.setTitle("Save Student Card");
            save.setInitialFileName("card-" + student.getStudentNum() + ".png");
            save.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG image", "*.png"));
            File file = save.showSaveDialog(main_form.getScene().getWindow());
            if (file == null) {
                return;
            }
            Path saved = StudentCards.withPngExtension(file.toPath());
            StudentCards.savePng(student, saved);
            showAlert(Alert.AlertType.INFORMATION, "Information Message",
                    "Saved the card for " + name + " to " + saved);
        } catch (ValidationException | UncheckedIOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    private void showPhoto(String storedName) {
        image = imageStore.find(storedName)
                .map(photo -> new Image(photo.toUri().toString(), 120, 170, false, true))
                .orElse(null);
        addStudent_imageView.setImage(image);
        chosenPhotoName = storedName;
    }
    private StudentData addStudentForm() {
        LocalDate birthDate = addStudent_birthDate.getValue();
        return new StudentData(studentService.parseStudentNumber(addStudent_studentNum.getText()),
                addStudent_semester.getSelectionModel().getSelectedItem(),
                addStudent_course.getSelectionModel().getSelectedItem(),
                addStudent_firstName.getText(),
                addStudent_lastName.getText(),
                addStudent_gender.getSelectionModel().getSelectedItem(),
                birthDate == null ? null : java.sql.Date.valueOf(birthDate),
                addStudent_status.getSelectionModel().getSelectedItem(),
                chosenPhotoName,
                addStudent_cardId.getText());
    }
    @FXML
    public void addStudent_addBtn_onAction(){
        try {
            studentService.add(addStudentForm());
            addStudentChanged("Successfully Added!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void addStudent_updateBtn_onAction () {
        try {
            StudentData student = addStudentForm();
            studentService.requireValid(student);
            if (!confirmed("Are you sure you want to update Student #: " + student.getStudentNum() + "?")) {
                return;
            }
            studentService.update(student);
            addStudentChanged("Successfully Updated!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void addStudent_deleteBtn_onAction () {
        try {
            int studentNum = studentService.parseStudentNumber(addStudent_studentNum.getText());
            if (!confirmed("Are you sure you want to delete Student #: " + studentNum
                    + " and their attendance records?")) {
                return;
            }
            studentService.delete(studentNum);
            addStudentChanged("Successfully Deleted!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    private void addStudentChanged(String successMessage) {
        showAlert(Alert.AlertType.INFORMATION, "Information Message", successMessage);
        addStudentShowListData();
        addStudent_clearBtn_onAction();
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
        addStudent_cardId.setText("");
        showPhoto("");

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
    private static final System.Logger LOG = System.getLogger(DashboardController.class.getName());
    private final CourseService courseService =
            new CourseService(new CourseDao(Database.fromCurrentSettings()));

    public ObservableList <CourseData> availableCourseListData(){
        try {
            return FXCollections.observableArrayList(courseService.listAll());
        } catch (DataAccessException e) {
            showDatabaseError(e);
            return FXCollections.observableArrayList();
        }
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
    private CourseData availableCourseForm() {
        return new CourseData(availableCourse_course.getText(),
                availableCourse_description.getText(),
                availableCourse_degree.getText());
    }
    @FXML
    public void availableCourse_addBtn_onAction () {
        try {
            courseService.add(availableCourseForm());
            availableCourseChanged("Successfully Added!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void availableCourse_updateBtn_onAction () {
        CourseData course = availableCourseForm();
        try {
            courseService.requireComplete(course);
            if (!confirmed("Are you sure you want to update Course: " + course.getCourse().strip() + "?")) {
                return;
            }
            courseService.update(course);
            availableCourseChanged("Successfully Updated!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void availableCourse_clearBtn_onAction () {
        availableCourse_course.setText("");
        availableCourse_description.setText("");
        availableCourse_degree.setText("");
    }
    @FXML
    public void availableCourse_deleteBtn_onAction() {
        String courseName = availableCourse_course.getText();
        try {
            courseService.requireCourseName(courseName);
            if (!confirmed("Are you sure you want to delete Course: " + courseName.strip() + "?")) {
                return;
            }
            courseService.delete(courseName);
            availableCourseChanged("Successfully Deleted!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    private void availableCourseChanged(String successMessage) {
        showAlert(Alert.AlertType.INFORMATION, "Information Message", successMessage);
        availableCourseShowListData();
        availableCourse_clearBtn_onAction();
    }
//    END CODE FOR AVAILABLE COURSES FORM

//    START CODE FOR STUDENT ATTENDANCE FORM
    private static final String SCAN_OK_STYLE = "scan-ok";
    private static final String SCAN_ERROR_STYLE = "scan-error";

    public ObservableList <StudentAttendanceData> studentAttendanceListData(){
        try {
            return FXCollections.observableArrayList(attendanceService.listToday());
        } catch (DataAccessException e) {
            showDatabaseError(e);
            return FXCollections.observableArrayList();
        }
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
        studentAttendance_semester.setItems(FXCollections.observableArrayList(semesterList));
        studentAttendance_semester.setOnAction(e -> studentAttendanceCourseList());
    }
    @FXML
    public void studentAttendanceCourseList(){
        String selectedSemester = studentAttendance_semester.getSelectionModel().getSelectedItem();
        try {
            studentAttendance_course.setItems(
                    FXCollections.observableArrayList(attendanceService.listCoursesIn(selectedSemester)));
            // A course of the previous semester would otherwise stay chosen
            studentAttendance_course.setValue(null);
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
        studentAttendance_course.setOnAction(e -> studentAttendanceStudentNumList());
    }
    @FXML
    public void studentAttendanceStudentNumList(){
        String selectedSemester = studentAttendance_semester.getSelectionModel().getSelectedItem();
        String selectedCourse = studentAttendance_course.getSelectionModel().getSelectedItem();
        try {
            List<String> studentNums =
                    attendanceService.listStudentNumbersIn(selectedSemester, selectedCourse).stream()
                            .map(String::valueOf)
                            .toList();
            studentAttendance_studentNum.setItems(FXCollections.observableArrayList(studentNums));
            studentAttendance_studentNum.setValue(null);
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    private Integer selectedAttendanceStudentNum() {
        String selected = studentAttendance_studentNum.getSelectionModel().getSelectedItem();
        return selected == null ? null : Integer.valueOf(selected);
    }
    @FXML
    public void studentAttendanceMarkEntryBtnOnAction() {
        try {
            attendanceService.markEntry(selectedAttendanceStudentNum());
            attendanceChanged("Entry marked for Student #: " + selectedAttendanceStudentNum());
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void studentAttendanceMarkExitOnAction() {
        try {
            int closed = attendanceService.markExit(selectedAttendanceStudentNum());
            attendanceChanged("Exit marked for Student #: " + selectedAttendanceStudentNum()
                    + (closed > 1 ? " (" + closed + " entries)" : ""));
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void studentAttendanceScanOnAction() {
        scanCard(studentAttendance_scanCard.getText());
        studentAttendance_scanCard.clear();
    }

    /** Checks the card's student in or out and shows what happened under the scan field. */
    public void scanCard(String cardId) {
        try {
            showScanResult(attendanceService.scan(cardId).message(), SCAN_OK_STYLE);
            studentAttendanceShowListData();
        } catch (ValidationException e) {
            showScanResult(e.getMessage(), SCAN_ERROR_STYLE);
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    private void showScanResult(String message, String style) {
        studentAttendance_scanResult.getStyleClass().removeAll(SCAN_OK_STYLE, SCAN_ERROR_STYLE);
        studentAttendance_scanResult.getStyleClass().add(style);
        studentAttendance_scanResult.setText(message);
    }
    private void attendanceChanged(String successMessage) {
        showAlert(Alert.AlertType.INFORMATION, "Information Message", successMessage);
        studentAttendanceShowListData();
        studentAttendanceClearBtnOnAction();
    }
    
    @FXML
    public void studentAttendanceClearBtnOnAction() {
        studentAttendance_semester.setValue(null);
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
    private final UserService userService =
            new UserService(new UserDao(Database.fromCurrentSettings()));

    public ObservableList<UserData> userManagementListData() {
        try {
            return FXCollections.observableArrayList(userService.listAll());
        } catch (DataAccessException e) {
            showDatabaseError(e);
            return FXCollections.observableArrayList();
        }
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
        UserData selectedUser = userManagement_tableView.getSelectionModel().getSelectedItem();

        if (selectedUser != null) {
            userManagement_id.setText(String.valueOf(selectedUser.getUserId()));
            userManagement_username.setText(selectedUser.getUserName());
            userManagement_userRole.getSelectionModel().select(selectedUser.getUserRole());
        }
    }

    @FXML
    public void userManagement_userRoleList() {
        userManagement_userRole.setItems(FXCollections.observableArrayList(UserService.ROLES));
    }
    private UserForm userManagementForm() {
        String chosenId = userManagement_id.getText();
        return new UserForm(
                chosenId == null || chosenId.isBlank() ? null : Integer.valueOf(chosenId),
                userManagement_username.getText(),
                userManagement_password.getText(),
                userManagement_confirmPassword.getText(),
                userManagement_userRole.getValue());
    }
    @FXML
    public void userManagementAddBtn_onAction() {
        UserForm form = userManagementForm();
        try {
            userService.add(form);
            userManagementChanged("User added successfully!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void userManagementUpdateBtn_onAction() {
        UserForm form = userManagementForm();
        try {
            userService.requireChosen(form.userId());
            userService.requireValid(form);
            if (!confirmed("Are you sure you want to update?")) {
                return;
            }
            userService.update(form);
            userManagementChanged("Updated Successfully");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    @FXML
    public void userManagementDeleteBtn_onAction() {
        UserForm form = userManagementForm();
        try {
            UserData chosen = userService.requireChosen(form.userId());
            if (!confirmed("Are you sure you want to delete User: " + chosen.getUserName() + "?")) {
                return;
            }
            userService.delete(chosen.getUserId(), UserSession.current().getUserName());
            userManagementChanged("Successfully Deleted!");
        } catch (ValidationException e) {
            showAlert(Alert.AlertType.ERROR, "Error Message", e.getMessage());
        } catch (DataAccessException e) {
            showDatabaseError(e);
        }
    }
    private void userManagementChanged(String successMessage) {
        showAlert(Alert.AlertType.INFORMATION, "Information Message", successMessage);
        userManagementShowListData();
        userManagementClearBtn_onAction();
    }
    private boolean confirmed(String question) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Message");
        alert.setHeaderText(null);
        alert.setContentText(question);
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
    private void showDatabaseError(DataAccessException e) {
        LOG.log(System.Logger.Level.WARNING, e.getMessage(), e);
        showAlert(Alert.AlertType.ERROR, "Database Error",
                e.getMessage() + ". Please try again; if it keeps happening, restart the app.");
    }
    private void showAlert(Alert.AlertType alertType, String title, String contentText) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(contentText);
        alert.showAndWait();
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
        initializeComboBoxes(studentAttendance_semester, studentAttendance_course, studentAttendance_studentNum);
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
