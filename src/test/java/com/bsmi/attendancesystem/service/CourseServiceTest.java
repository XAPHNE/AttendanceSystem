package com.bsmi.attendancesystem.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bsmi.attendancesystem.CourseData;
import com.bsmi.attendancesystem.dao.CourseDao;
import com.bsmi.attendancesystem.dao.DataAccessException;
import com.bsmi.attendancesystem.dao.TestDatabases;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CourseServiceTest {
    private CourseService service;

    @BeforeEach
    void useFreshDatabase() {
        service = new CourseService(new CourseDao(TestDatabases.migratedInMemory()));
    }

    @Test
    void listAllReturnsStoredCourses() {
        assertThat(service.listAll()).hasSize(3);
    }

    @Test
    void addStoresTrimmedValues() {
        service.add(new CourseData("  BBA ", " Business  ", " Bachelor "));

        CourseData added = service.listAll().getLast();
        assertThat(added.getCourse()).isEqualTo("BBA");
        assertThat(added.getDescription()).isEqualTo("Business");
        assertThat(added.getDegree()).isEqualTo("Bachelor");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void addWithBlankCourseNamesTheCourseField(String blank) {
        assertThatThrownBy(() -> service.add(new CourseData(blank, "d", "g")))
                .isInstanceOf(MissingFieldException.class)
                .hasMessage("Please fill in the Course field")
                .extracting(e -> ((MissingFieldException) e).fieldName())
                .isEqualTo("Course");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void addWithBlankDescriptionNamesTheDescriptionField(String blank) {
        assertThatThrownBy(() -> service.add(new CourseData("BBA", blank, "g")))
                .isInstanceOf(MissingFieldException.class)
                .hasMessageContaining("Description");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void addWithBlankDegreeNamesTheDegreeField(String blank) {
        assertThatThrownBy(() -> service.add(new CourseData("BBA", "d", blank)))
                .isInstanceOf(MissingFieldException.class)
                .hasMessageContaining("Degree");
    }

    @Test
    void addWithEverythingBlankReportsTheFirstField() {
        assertThatThrownBy(() -> service.add(new CourseData("", "", "")))
                .hasMessageContaining("Course");
    }

    @Test
    void addingExistingNameFailsWithAlreadyExists() {
        assertThatThrownBy(() -> service.add(new CourseData("BCA", "d", "g")))
                .isInstanceOf(AlreadyExistsException.class)
                .hasMessage("Course BCA already exists");
    }

    @Test
    void addingExistingNameWithSpacesStillFailsWithAlreadyExists() {
        assertThatThrownBy(() -> service.add(new CourseData(" BCA ", "d", "g")))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    void failedAddStoresNothing() {
        assertThatThrownBy(() -> service.add(new CourseData("BBA", "", "g")))
                .isInstanceOf(MissingFieldException.class);

        assertThat(service.listAll()).hasSize(3);
    }

    @Test
    void updateChangesTheCourse() {
        service.update(new CourseData(" MCA ", " Master ", " PG "));

        CourseData mca = service.listAll().getLast();
        assertThat(mca.getDescription()).isEqualTo("Master");
        assertThat(mca.getDegree()).isEqualTo("PG");
    }

    @Test
    void updateOfUnknownCourseFailsWithNotFound() {
        assertThatThrownBy(() -> service.update(new CourseData("PhD", "d", "g")))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course PhD does not exist");
    }

    @Test
    void updateWithBlankFieldFailsWithMissingField() {
        assertThatThrownBy(() -> service.update(new CourseData("MCA", "d", " ")))
                .isInstanceOf(MissingFieldException.class)
                .hasMessageContaining("Degree");
    }

    @Test
    void deleteRemovesUnusedCourse() {
        service.add(new CourseData("BBA", "Business", "Bachelor"));

        service.delete(" BBA ");

        assertThat(service.listAll()).extracting(CourseData::getCourse).doesNotContain("BBA");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void deleteWithBlankNameFailsWithMissingField(String blank) {
        assertThatThrownBy(() -> service.delete(blank))
                .isInstanceOf(MissingFieldException.class)
                .hasMessageContaining("Course");
    }

    @Test
    void deleteOfUnknownCourseFailsWithNotFound() {
        assertThatThrownBy(() -> service.delete("PhD"))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Course PhD does not exist");
    }

    @Test
    void deleteOfCourseWithStudentsFailsWithStillInUse() {
        assertThatThrownBy(() -> service.delete("BCA"))
                .isInstanceOf(StillInUseException.class)
                .hasMessage(
                        "Course BCA can't be deleted while students or attendance records use it");
        assertThat(service.listAll()).extracting(CourseData::getCourse).contains("BCA");
    }

    @Test
    void requireCompleteAcceptsFilledForm() {
        assertThatCode(() -> service.requireComplete(new CourseData("a", "b", "c")))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCourseNameRejectsBlankAndAcceptsText() {
        assertThatThrownBy(() -> service.requireCourseName(" "))
                .isInstanceOf(MissingFieldException.class);
        assertThatCode(() -> service.requireCourseName("BCA")).doesNotThrowAnyException();
    }

    @Test
    void validationErrorsAreValidationExceptions() {
        assertThatThrownBy(() -> service.delete("")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.delete("PhD")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.delete("BCA")).isInstanceOf(ValidationException.class);
    }

    @Test
    void databaseFailuresPassThroughAsDataAccessException() {
        CourseService offline = new CourseService(new CourseDao(TestDatabases.unreachable()));

        assertThatThrownBy(offline::listAll).isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> offline.add(new CourseData("a", "b", "c")))
                .isInstanceOf(DataAccessException.class);
    }
}
