package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.StudentData;
import com.bsmi.attendancesystem.dao.DataAccessException;
import com.bsmi.attendancesystem.dao.StudentDao;
import com.bsmi.attendancesystem.dao.TestDatabases;
import java.sql.Date;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class StudentServiceTest {
    private static final Date BIRTH_DATE = Date.valueOf("2006-05-01");

    private StudentService service;

    @BeforeEach
    void useFreshDatabase() {
        service = new StudentService(new StudentDao(TestDatabases.migratedInMemory()));
    }

    @Test
    void listAllReturnsStoredStudents() {
        assertThat(service.listAll()).hasSize(6);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "2001", " 42 ", "2147483647"})
    void parseStudentNumberAcceptsPositiveWholeNumbers(String text) {
        assertThat(service.parseStudentNumber(text)).isEqualTo(Integer.parseInt(text.strip()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void parseStudentNumberOfBlankTextNamesTheField(String blank) {
        assertThatThrownBy(() -> service.parseStudentNumber(blank))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Student # field");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "12a", "1.5", "2147483648", "x' OR '1'='1"})
    void parseStudentNumberRejectsAnythingElse(String text) {
        assertThatThrownBy(() -> service.parseStudentNumber(text))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Student # must be a positive whole number")
                .extracting(e -> ((InvalidValueException) e).fieldName())
                .isEqualTo("Student #");
    }

    @Test
    void addStoresTrimmedValues() {
        service.add(
                new StudentData(
                        2001, " 1st Sem ", "BSc", "  Zoya ", " Ali", "Female", BIRTH_DATE,
                        "Enrolled ", " "));

        StudentData added = service.listAll().getLast();
        assertThat(added.getSemester()).isEqualTo("1st Sem");
        assertThat(added.getFirstName()).isEqualTo("Zoya");
        assertThat(added.getLastName()).isEqualTo("Ali");
        assertThat(added.getStatus()).isEqualTo("Enrolled");
        assertThat(added.getImage()).isEmpty();
    }

    @Test
    void addWithoutPhotoIsAllowed() {
        service.add(valid(2001).withImage(null).build());

        assertThat(service.listAll().getLast().getImage()).isEmpty();
    }

    static Stream<Arguments> formsWithOneEmptyField() {
        return Stream.of(
                Arguments.of(valid(2001).withStudentNum(null), "Student #"),
                Arguments.of(valid(2001).withSemester(null), "Semester"),
                Arguments.of(valid(2001).withCourse(" "), "Course"),
                Arguments.of(valid(2001).withFirstName(""), "First Name"),
                Arguments.of(valid(2001).withLastName("  "), "Last Name"),
                Arguments.of(valid(2001).withGender(null), "Gender"),
                Arguments.of(valid(2001).withBirthDate(null), "Birth Date"),
                Arguments.of(valid(2001).withStatus(null), "Status"));
    }

    @ParameterizedTest
    @MethodSource("formsWithOneEmptyField")
    void addWithAnEmptyFieldNamesThatField(Form form, String fieldName) {
        assertThatThrownBy(() -> service.add(form.build()))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the " + fieldName + " field");
        assertThat(service.listAll()).hasSize(6);
    }

    @Test
    void addWithEverythingEmptyReportsTheFirstField() {
        StudentData empty = new StudentData(null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> service.add(empty)).hasMessageContaining("Student #");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 365})
    void addWithBirthDateTodayOrLaterFailsWithInvalidValue(long daysFromToday) {
        Date notInThePast = Date.valueOf(LocalDate.now().plusDays(daysFromToday));

        assertThatThrownBy(() -> service.add(valid(2001).withBirthDate(notInThePast).build()))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Birth Date must be in the past");
    }

    @Test
    void addWithBirthDateYesterdayIsAllowed() {
        Date yesterday = Date.valueOf(LocalDate.now().minusDays(1));

        assertThatCode(() -> service.add(valid(2001).withBirthDate(yesterday).build()))
                .doesNotThrowAnyException();
    }

    @Test
    void addingTakenNumberFailsWithAlreadyExists() {
        assertThatThrownBy(() -> service.add(valid(1001).build()))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Student #1001 already exists");
    }

    @Test
    void addWithoutCardIsAllowed() {
        service.add(valid(2001).build());

        assertThat(service.listAll().getLast().getCardId()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void addWithBlankCardStoresNoCard(String blank) {
        service.add(valid(2001).withCardId(blank).build());

        assertThat(service.listAll().getLast().getCardId()).isNull();
    }

    @Test
    void addStoresTheCardIdTrimmed() {
        service.add(valid(2001).withCardId("  STU-7F3A \n").build());

        assertThat(service.listAll().getLast().getCardId()).isEqualTo("STU-7F3A");
    }

    @Test
    void addWithCardTakenByAnotherStudentFailsWithAlreadyExists() {
        assertThatThrownBy(() -> service.add(valid(2001).withCardId(" DEMO-1001 ").build()))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Card ID DEMO-1001 already exists");
        assertThat(service.listAll()).hasSize(6);
    }

    @Test
    void addWithCardIdOfMaximumLengthIsAllowed() {
        String longest = "C".repeat(StudentService.CARD_ID_MAX_LENGTH);

        service.add(valid(2001).withCardId(longest).build());

        assertThat(service.listAll().getLast().getCardId()).isEqualTo(longest);
    }

    @Test
    void addWithTooLongCardIdFailsWithInvalidValue() {
        String tooLong = "C".repeat(StudentService.CARD_ID_MAX_LENGTH + 1);

        assertThatThrownBy(() -> service.add(valid(2001).withCardId(tooLong).build()))
                .isInstanceOf(InvalidValueException.class)
                .hasMessage("Card ID must be at most 64 characters long");
    }

    @Test
    void updateKeepingOwnCardIsAllowed() {
        service.update(valid(1001).withCardId("DEMO-1001").withStatus("Graduated").build());

        assertThat(service.listAll().getFirst().getCardId()).isEqualTo("DEMO-1001");
        assertThat(service.listAll().getFirst().getStatus()).isEqualTo("Graduated");
    }

    @Test
    void updateToCardOfAnotherStudentFailsWithAlreadyExists() {
        assertThatThrownBy(() -> service.update(valid(1002).withCardId("DEMO-1001").build()))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Card ID DEMO-1001 already exists");
        assertThat(service.listAll().get(1).getCardId()).isEqualTo("DEMO-1002");
    }

    @Test
    void updateWithBlankCardRemovesTheCard() {
        service.update(valid(1001).withCardId(" ").build());

        assertThat(service.listAll().getFirst().getCardId()).isNull();
    }

    @Test
    void updateChangesTheStudent() {
        service.update(valid(1001).withFirstName(" Asha ").withStatus("Dropped Out").build());

        StudentData asha = service.listAll().getFirst();
        assertThat(asha.getFirstName()).isEqualTo("Asha");
        assertThat(asha.getStatus()).isEqualTo("Dropped Out");
    }

    @Test
    void updateOfUnknownNumberFailsWithNotFound() {
        assertThatThrownBy(() -> service.update(valid(9999).build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Student #9999 does not exist");
    }

    @Test
    void updateWithEmptyFieldFailsBeforeTouchingTheDatabase() {
        assertThatThrownBy(() -> service.update(valid(1001).withLastName("").build()))
                .isInstanceOf(MissingFieldException.class);
        assertThat(service.listAll().getFirst().getLastName()).isEqualTo("Verma");
    }

    @Test
    void issueCardGivesTheStudentANewSavedCardId() {
        StudentData noCard = valid(2001).build();
        service.add(noCard);

        StudentData issued = service.issueCard(noCard);

        assertThat(issued.getCardId()).startsWith("STU-");
        assertThat(service.listAll().getLast().getCardId()).isEqualTo(issued.getCardId());
        assertThat(issued.getFirstName()).isEqualTo("Zoya");
    }

    @Test
    void issueCardForUnknownStudentFailsWithNotFound() {
        assertThatThrownBy(() -> service.issueCard(valid(9999).build()))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Student #9999 does not exist");
    }

    @Test
    void deleteRemovesTheStudent() {
        service.delete(1001);

        assertThat(service.listAll()).extracting(StudentData::getStudentNum).doesNotContain(1001);
    }

    @Test
    void deleteOfUnknownNumberFailsWithNotFound() {
        assertThatThrownBy(() -> service.delete(9999))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Student #9999 does not exist");
    }

    @Test
    void requireValidAcceptsACompleteForm() {
        assertThatCode(() -> service.requireValid(valid(2001).build())).doesNotThrowAnyException();
    }

    @Test
    void databaseFailuresPassThroughAsDataAccessException() {
        StudentService offline = new StudentService(new StudentDao(TestDatabases.unreachable()));

        assertThatThrownBy(offline::listAll).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.add(valid(2001).build()))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.delete(2001)).isInstanceOf(DataAccessException.class);
    }

    private static Form valid(Integer studentNum) {
        return new Form(
                studentNum, "1st Sem", "BSc", "Zoya", "Ali", "Female", BIRTH_DATE, "Enrolled", "",
                null);
    }

    /** A filled-in Add Student form; each {@code with} call empties or changes one field. */
    record Form(
            Integer studentNum,
            String semester,
            String course,
            String firstName,
            String lastName,
            String gender,
            Date birthDate,
            String status,
            String image,
            String cardId) {
        Form withStudentNum(Integer value) {
            return new Form(
                    value, semester, course, firstName, lastName, gender, birthDate, status, image, cardId);
        }

        Form withSemester(String value) {
            return new Form(
                    studentNum, value, course, firstName, lastName, gender, birthDate, status, image, cardId);
        }

        Form withCourse(String value) {
            return new Form(
                    studentNum, semester, value, firstName, lastName, gender, birthDate, status, image, cardId);
        }

        Form withFirstName(String value) {
            return new Form(
                    studentNum, semester, course, value, lastName, gender, birthDate, status, image, cardId);
        }

        Form withLastName(String value) {
            return new Form(
                    studentNum, semester, course, firstName, value, gender, birthDate, status, image, cardId);
        }

        Form withGender(String value) {
            return new Form(
                    studentNum, semester, course, firstName, lastName, value, birthDate, status, image, cardId);
        }

        Form withBirthDate(Date value) {
            return new Form(
                    studentNum, semester, course, firstName, lastName, gender, value, status, image, cardId);
        }

        Form withStatus(String value) {
            return new Form(
                    studentNum, semester, course, firstName, lastName, gender, birthDate, value, image, cardId);
        }

        Form withImage(String value) {
            return new Form(
                    studentNum, semester, course, firstName, lastName, gender, birthDate, status, value, cardId);
        }

        Form withCardId(String value) {
            return new Form(
                    studentNum, semester, course, firstName, lastName, gender, birthDate, status, image, value);
        }

        StudentData build() {
            return new StudentData(
                    studentNum, semester, course, firstName, lastName, gender, birthDate, status, image,
                    cardId);
        }
    }
}
