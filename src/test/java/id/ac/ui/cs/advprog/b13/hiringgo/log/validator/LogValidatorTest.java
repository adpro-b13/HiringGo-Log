package id.ac.ui.cs.advprog.b13.hiringgo.log.validator;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LogValidatorTest {

    private final LogValidator validator = new LogValidator();

    @Test
    void unhappy_validateShouldFailForBlankTitle() {
        Log log = new Log("", "Description", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        LogValidationException exception = assertThrows(LogValidationException.class, () -> {
            validator.validate(log);
        });
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailWhenStartTimeNotBeforeEndTime() {
        LocalDateTime time = LocalDateTime.now();
        Log log = new Log("Test", "Description", "Asistensi",
                time.plusHours(2), time.plusHours(1), LocalDate.now());
        LogValidationException exception = assertThrows(LogValidationException.class, () -> {
            validator.validate(log);
        });
        assertEquals("Waktu mulai harus sebelum waktu selesai.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailForFutureLogDate() {
        Log log = new Log("Test", "Description", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now().plusDays(1));
        LogValidationException exception = assertThrows(LogValidationException.class, () -> {
            validator.validate(log);
        });
        assertEquals("Tanggal log tidak boleh di masa depan.", exception.getMessage());
    }

    @Test
    void unhappy_validateShouldFailForLongDuration() {
        Log log = new Log("Test", "Description", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(13), LocalDate.now());
        LogValidationException exception = assertThrows(LogValidationException.class, () -> {
            validator.validate(log);
        });
        assertEquals("Durasi log tidak boleh lebih dari 12 jam.", exception.getMessage());
    }
}

