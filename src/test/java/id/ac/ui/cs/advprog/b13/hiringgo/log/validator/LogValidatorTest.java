package id.ac.ui.cs.advprog.b13.hiringgo.log.validator;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class LogValidatorTest {

    private final LogValidator validator = new LogValidator();
    private Log log;

    private Log createValidLog() {
        return new Log(
                "Valid Title",
                "Valid Description",
                "Valid Category",
                123L, 
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().minusHours(1),
                LocalDate.now().minusDays(1),
                1L // Changed to Long
        );
    }

    @BeforeEach
    void setUp() {
        log = createValidLog();
    }

    @Test
    void happy_validateShouldPassWithValidLog() {
        assertDoesNotThrow(() -> validator.validate(log));
    }

    @Test
    void unhappy_validateShouldFailWhenStudentIdIsNull() {
        log.setStudentId(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Student ID tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenTitleIsNull() {
        log.setTitle(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenTitleIsEmpty() {
        log.setTitle("");
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenTitleIsBlank() {
        log.setTitle("   ");
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenTitleExceedsMaxLength() {
        String longTitle = String.join("", Collections.nCopies(256, "a"));
        log.setTitle(longTitle);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Judul log tidak boleh lebih dari 255 karakter.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenDescriptionIsNull() {
        log.setDescription(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Deskripsi log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenDescriptionIsEmpty() {
        log.setDescription("");
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Deskripsi log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenDescriptionIsBlank() {
        log.setDescription("   ");
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Deskripsi log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenDescriptionExceedsMaxLength() {
        String longDescription = String.join("", Collections.nCopies(1001, "a"));
        log.setDescription(longDescription);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Deskripsi log tidak boleh lebih dari 1000 karakter.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenCategoryIsNull() {
        log.setCategory(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Kategori tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenCategoryIsEmpty() {
        log.setCategory("");
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Kategori tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenCategoryIsBlank() {
        log.setCategory("   ");
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Kategori tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenVacancyIdIsNull() {
        log.setVacancyId(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("ID lowongan tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenStartTimeIsNull() {
        log.setStartTime(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Waktu mulai harus diisi.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenEndTimeIsNull() {
        log.setEndTime(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Waktu selesai harus diisi.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenLogDateIsNull() {
        log.setLogDate(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Tanggal log harus diisi.", exception.getMessage());
    }
    
    @Test
    void unhappy_validateShouldFailWhenStatusIsNull() {
        log.setStatus(null);
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Status tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenStartTimeNotBeforeEndTime() {
        LocalDateTime time = LocalDateTime.now();
        log.setStartTime(time.plusHours(2));
        log.setEndTime(time.plusHours(1));
        LogValidationException exception = assertThrows(LogValidationException.class, () -> {
            validator.validate(log);
        });
        assertEquals("Waktu mulai harus sebelum waktu selesai.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenLogDateIsInTheFuture() {
        log.setLogDate(LocalDate.now().plusDays(1));
        LogValidationException exception = assertThrows(LogValidationException.class, () -> validator.validate(log));
        assertEquals("Tanggal log tidak boleh di masa depan.", exception.getMessage());
    }
}

