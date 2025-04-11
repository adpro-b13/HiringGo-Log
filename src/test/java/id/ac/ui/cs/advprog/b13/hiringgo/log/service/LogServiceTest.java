package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.InMemoryLogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidationException;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LogServiceTest {

    private LogService logService;
    private InMemoryLogRepository repository;
    private LogValidator validator;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLogRepository();
        validator = new LogValidator();
        logService = new LogService(repository, validator);
    }

    @Test
    void red_createLogShouldThrowWhenTitleIsBlank() {
        Log log = new Log("", "Description", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        LogValidationException exception = assertThrows(LogValidationException.class,
                () -> logService.createLog(log));
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
    }

    @Test
    void green_createLogShouldPersistValidLog() {
        Log log = new Log("Valid Log", "Proper log", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        Log saved = logService.createLog(log);
        assertNotNull(saved.getId());
        assertEquals(LogStatus.REPORTED, saved.getStatus());
    }

    @Test
    void red_updateLogShouldNotAllowUpdateWhenStatusNotReported() {
        Log log = new Log("Log to Update", "Will be verified", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        Log saved = logService.createLog(log);
        logService.verifyLog(saved.getId(), VerificationAction.ACCEPT); // now status is ACCEPTED
        saved.setTitle("Updated Title");
        assertThrows(IllegalStateException.class, () -> logService.updateLog(saved));
    }

    @Test
    void green_updateLogShouldSucceedWhenStatusReported() {
        // Create a valid log (status is REPORTED by default).
        Log log = new Log("Log to Update", "Initial description", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        Log saved = logService.createLog(log);

        // Modify some field on the log.
        saved.setTitle("Updated Title");
        saved.setDescription("Updated description");

        // Update should succeed because the status is still REPORTED.
        Log updated = logService.updateLog(saved);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(LogStatus.REPORTED, updated.getStatus());
    }

    @Test
    void green_deleteLogShouldRemoveLog() {
        // Create a log that can be deleted.
        Log log = new Log("Log to Delete", "Some description", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        Log saved = logService.createLog(log);
        Long id = saved.getId();
        assertNotNull(id);

        // Delete the log.
        logService.deleteLog(id);

        // Verify that the log is no longer present.
        List<Log> logs = logService.getAllLogs();
        assertTrue(logs.stream().noneMatch(l -> l.getId().equals(id)));
    }

    @Test
    void green_getAllLogsShouldReturnAllSavedLogs() {
        Log log1 = new Log("Log 1", "Description 1", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        Log log2 = new Log("Log 2", "Description 2", "Responsi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());

        logService.createLog(log1);
        logService.createLog(log2);

        List<Log> logs = logService.getAllLogs();

        assertEquals(2, logs.size());
        assertTrue(logs.stream().anyMatch(log -> log.getTitle().equals("Log 1")));
        assertTrue(logs.stream().anyMatch(log -> log.getTitle().equals("Log 2")));
    }

    @Test
    void green_verifyLogShouldChangeStatusToAccepted() {
        Log log = new Log("Log for Verification", "To be verified", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        Log saved = logService.createLog(log);
        Log verified = logService.verifyLog(saved.getId(), VerificationAction.ACCEPT);
        assertEquals(LogStatus.ACCEPTED, verified.getStatus());
    }
    
    @Test
    void red_verifyLogShouldChangeStatusToRejected() {
        Log log = new Log("Log for Rejection", "Needs rejection", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        Log saved = logService.createLog(log);
        Log verified = logService.verifyLog(saved.getId(), VerificationAction.REJECT);
        assertEquals(LogStatus.REJECTED, verified.getStatus());
    }

    @Test
    void red_verifyLogShouldThrowWhenLogAlreadyVerified() {
        Log log = new Log("Already Verified", "Should not verify again", "Asistensi",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        Log saved = logService.createLog(log);
        // First verify as ACCEPTED.
        logService.verifyLog(saved.getId(), VerificationAction.ACCEPT);
        // Then, attempt a second verification should throw an exception.
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.verifyLog(saved.getId(), VerificationAction.REJECT));
        assertTrue(exception.getMessage().contains("Log sudah diverifikasi"));
    }

}
