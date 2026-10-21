package com.bsmi.attendancesystem.ui;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.Test;

class LoginScreenTest extends AppScreenTest {
    @Test
    void formStartsEmptyAndShowsTheDemoLogins() {
        assertThat(lookup("#usernameMFXTextField").queryAs(MFXTextField.class).getText()).isEmpty();
        assertThat(lookup("#demoAccountsHint").queryAs(Label.class).getText())
                .contains("admin / admin123", "employee / employee123");
    }

    @Test
    void blankFormAsksForTheUsername() {
        clickOn("#loginButton");

        assertThat(alertText()).isEqualTo("Please fill in the Username field");
    }

    @Test
    void missingPasswordIsAskedFor() {
        clickOn("#usernameMFXTextField").write("admin");
        clickOn("#loginButton");

        assertThat(alertText()).isEqualTo("Please fill in the Password field");
    }

    @Test
    void wrongPasswordIsRejected() {
        logInAs("admin", "not-the-password");

        assertThat(alertText()).isEqualTo("Incorrect Username/Password");
    }

    @Test
    void unknownUserGetsTheSameMessageAsAWrongPassword() {
        logInAs("nobody", "admin123");

        assertThat(alertText()).isEqualTo("Incorrect Username/Password");
    }

    @Test
    void adminLandsOnTheDashboardWithUserManagement() {
        logInAs("admin", "admin123");
        assertThat(alertText()).isEqualTo("Login Successful");
        pressAlertButton("OK");

        assertThat(lookup("#username").queryAs(Label.class).getText()).isEqualTo("admin");
        assertThat(lookup("#userManagement_btn").queryAs(Button.class).isVisible()).isTrue();
    }

    @Test
    void employeeDoesNotSeeUserManagement() {
        logInAndDismissWelcome("employee", "employee123");

        assertThat(lookup("#userManagement_btn").queryAs(Button.class).isVisible()).isFalse();
    }

    @Test
    void enterKeyLogsIn() {
        clickOn("#usernameMFXTextField").write("admin");
        clickOn("#passwordMFXPasswordField").write("admin123").type(KeyCode.ENTER);

        assertThat(alertText()).isEqualTo("Login Successful");
    }
}
