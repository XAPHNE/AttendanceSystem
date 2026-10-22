package com.bsmi.attendancesystem.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.bsmi.attendancesystem.StudentAttendanceData;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

/** A card reader types the card ID and presses Enter; these tests do the same. */
class CardScanTest extends AppScreenTest {
    // The service ignores the same card again for 5 seconds
    private static final long PAST_REPEAT_WINDOW_MS = 5_500;
    private static final int DEMO_RECORDS_TODAY = 5;

    @BeforeEach
    void openAttendance() {
        logInAndDismissWelcome("admin", "admin123");
        clickOn("#studentAttendance_btn");
        WaitForAsyncUtils.waitForFxEvents();
    }

    @Test
    void scanFieldHasFocusSoAReaderCanTypeRightAway() {
        assertThat(scanField().isFocused()).isTrue();
    }

    @Test
    void scanningADemoCardChecksTheStudentIn() {
        scan("DEMO-1001");

        assertThat(scanResult()).startsWith("Asha Verma checked in ");
        assertThat(records()).hasSize(DEMO_RECORDS_TODAY + 1);
        assertThat(records().getLast().getStudentNum()).isEqualTo("1001");
        assertThat(records().getLast().getExitTime()).isNull();
        assertThat(scanField().getText()).isEmpty();
    }

    @Test
    void scanningTheSameCardLaterChecksTheStudentOut() {
        scan("DEMO-1001");
        scan("DEMO-1001");
        assertThat(scanResult()).isEqualTo("Asha Verma was just scanned");

        sleep(PAST_REPEAT_WINDOW_MS);
        scan("DEMO-1001");

        assertThat(scanResult()).startsWith("Asha Verma checked out ");
        assertThat(records()).hasSize(DEMO_RECORDS_TODAY + 1);
        assertThat(records().getLast().getExitTime()).isNotNull();
    }

    @Test
    void scanningAnUnknownCardShowsWhyAndAddsNoRecord() {
        scan("NOPE-1");

        assertThat(scanResult()).isEqualTo("No student has card NOPE-1");
        assertThat(records()).hasSize(DEMO_RECORDS_TODAY);
        assertThat(scanField().getText()).isEmpty();
    }

    private void scan(String cardId) {
        clickOn(scanField()).write(cardId).type(KeyCode.ENTER);
    }

    private TextField scanField() {
        return lookup("#studentAttendance_scanCard").queryAs(TextField.class);
    }

    private String scanResult() {
        return lookup("#studentAttendance_scanResult").queryAs(Label.class).getText();
    }

    private List<StudentAttendanceData> records() {
        TableView<StudentAttendanceData> table =
                lookup("#studentAttendance_tableView").queryTableView();
        return table.getItems();
    }
}
