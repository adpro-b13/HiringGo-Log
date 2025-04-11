package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

class LogModelTest {

    // RED: Write a test that a new Log has status REPORTED by default.
    @Test
    void red_newLogShouldHaveDefaultStatusReported() {
        Log log = new Log("Title", "Desc", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        // Expect the default status to be REPORTED.
        assertEquals(LogStatus.REPORTED, log.getStatus());
    }
}
