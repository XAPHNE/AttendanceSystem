package com.bsmi.attendancesystem.ui;

import com.bsmi.attendancesystem.AppPaths;
import com.bsmi.attendancesystem.AttendanceApplication;
import com.bsmi.attendancesystem.UserSession;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.testfx.api.FxRobot;
import org.testfx.api.FxToolkit;

/** Starts the real app on a fresh demo database for every test, then closes all its windows. */
@UiTest
abstract class AppScreenTest extends FxRobot {
    @TempDir Path dataFolder;

    @BeforeAll
    static void startJavaFx() throws Exception {
        FxToolkit.registerPrimaryStage();
    }

    @BeforeEach
    void startApp() throws Exception {
        System.setProperty(AppPaths.HOME_OVERRIDE_PROPERTY, dataFolder.toString());
        FxToolkit.setupFixture(
                () -> {
                    AttendanceApplication app = new AttendanceApplication();
                    app.init();
                    // The app styles its window, which only works on a stage never shown before
                    app.start(new Stage());
                    return null;
                });
    }

    @AfterEach
    void closeApp() {
        interact(() -> List.copyOf(Window.getWindows()).forEach(Window::hide));
        UserSession.end();
        System.clearProperty(AppPaths.HOME_OVERRIDE_PROPERTY);
    }

    void logInAs(String username, String password) {
        clickOn("#usernameMFXTextField").write(username);
        clickOn("#passwordMFXPasswordField").write(password);
        clickOn("#loginButton");
    }

    void logInAndDismissWelcome(String username, String password) {
        logInAs(username, password);
        pressAlertButton("OK");
    }

    String alertText() {
        return lookup(".dialog-pane").queryAs(DialogPane.class).getContentText();
    }

    void pressAlertButton(String text) {
        Button button =
                from(lookup(".dialog-pane").queryAs(DialogPane.class))
                        .lookup((Button candidate) -> text.equals(candidate.getText()))
                        .query();
        clickOn(button);
    }
}
