package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidationException;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogServiceTest {

    @InjectMocks
    private LogServiceImpl logService;

    @Mock
    private LogRepository repository;

    @Mock
    private LogValidator validator;

    @Mock 
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void unhappy_createLogShouldThrowWhenTitleIsBlank() {
        Log log = new Log("", "Description", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        doThrow(new LogValidationException("Judul log tidak boleh kosong.")).when(validator).validate(log);

        LogValidationException exception = assertThrows(LogValidationException.class,
                () -> logService.createLog(log));
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
        verify(validator).validate(log);
        verifyNoInteractions(repository);
    }

    @Test
    void happy_createLogShouldPersistValidLog() {
        Log log = new Log("Valid Log", "Proper log", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());

        doNothing().when(validator).validate(log);
        when(repository.save(log)).thenReturn(log);

        Log result = logService.createLog(log);
        
        assertEquals(LogStatus.REPORTED, result.getStatus());
        verify(validator).validate(log);
        verify(repository).save(log);
    }

    @Test
    void unhappy_createLogShouldThrowWhenVacancyIdIsNull() {
        Log log = new Log("Valid Log", "Proper log", "Teaching Assistant", null,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());

        doThrow(new LogValidationException("Id lowongan tidak boleh kosong.")).when(validator).validate(log);

        LogValidationException exception = assertThrows(LogValidationException.class,
                () -> logService.createLog(log));
        assertEquals("Id lowongan tidak boleh kosong.", exception.getMessage());
        verify(validator).validate(log);
        verifyNoInteractions(repository);
    }

    @Test
    void unhappy_updateLogShouldNotAllowUpdateWhenStatusNotReported() {
        Long id = 1L;
        Log existing = new Log("Log to Update", "Will be verified", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        existing.setId(id);
        existing.setStatus(LogStatus.ACCEPTED);

        when(repository.findById(id)).thenReturn(existing);

        Log toUpdate = new Log("Log to Update", "Will be verified", "Asistensi", "VAC-2024-1",
                existing.getStartTime(), existing.getEndTime(), existing.getLogDate());
        toUpdate.setId(id);

        assertThrows(IllegalStateException.class, () -> logService.updateLog(toUpdate));
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void happy_updateLogShouldSucceedWhenStatusReported() {
        Long id = 2L;
        Log existing = new Log("Log to Update", "Initial description", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        existing.setId(id);
        existing.setStatus(LogStatus.REPORTED);

        when(repository.findById(id)).thenReturn(existing);
        doNothing().when(validator).validate(existing);
        when(repository.save(existing)).thenReturn(existing);

        existing.setTitle("Updated Title");
        existing.setDescription("Updated description");

        Log updated = logService.updateLog(existing);
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(LogStatus.REPORTED, updated.getStatus());
        verify(repository).findById(id);
        verify(validator).validate(existing);
        verify(repository).save(existing);
    }

    @Test
    void happy_deleteLogShouldRemoveLog() {
        Long id = 3L;
        Log existing = new Log("Log to Delete", "Some description", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        existing.setId(id);
        existing.setStatus(LogStatus.REPORTED);

        when(repository.findById(id)).thenReturn(existing);
        doNothing().when(repository).delete(existing);

        logService.deleteLog(id);
        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void happy_verifyLogShouldChangeStatusToAccepted() {
        Long id = 4L;
        Log existing = new Log("Log for Verification", "To be verified", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        existing.setId(id);
        existing.setStatus(LogStatus.REPORTED);

        when(repository.findById(id)).thenReturn(existing);
        when(repository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        Log verified = logService.verifyLog(id, VerificationAction.ACCEPT);
        assertEquals(LogStatus.ACCEPTED, verified.getStatus());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void unhappy_verifyLogShouldChangeStatusToRejected() {
        Long id = 5L;
        Log existing = new Log("Log for Rejection", "Needs rejection", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        existing.setId(id);
        existing.setStatus(LogStatus.REPORTED);

        when(repository.findById(id)).thenReturn(existing);
        when(repository.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        Log rejected = logService.verifyLog(id, VerificationAction.REJECT);
        assertEquals(LogStatus.REJECTED, rejected.getStatus());
        verify(repository).findById(id);
        verify(repository).save(existing);
    }

    @Test
    void unhappy_verifyLogShouldThrowWhenAlreadyVerified() {
        Long id = 6L;
        Log existing = new Log("Already Verified", "Should not verify again", "Asistensi", "VAC-2024-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        existing.setId(id);
        existing.setStatus(LogStatus.ACCEPTED);

        when(repository.findById(id)).thenReturn(existing);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.verifyLog(id, VerificationAction.REJECT));
        assertTrue(exception.getMessage().contains("Log sudah diverifikasi"));
        verify(repository).findById(id);
    }

    @Test
    void happy_getAllLogsOnlyReturnsMine() {
        // 1) Arrange: two logs, one “mine” and one “theirs”
        Log mine   = new Log("T1", "D1", "C", "VAC", LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), "user-123");
        Log theirs = new Log("T2", "D2", "C", "VAC", LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), "other-456");
        when(userService.getCurrentStudentId()).thenReturn("user-123");
        when(repository.findAll()).thenReturn(List.of(mine, theirs));

        // 2) Act
        List<Log> result = logService.getAllLogs();

        // 3) Assert
        assertEquals(1, result.size());
        assertEquals("user-123", result.get(0).getStudentId());
        verify(userService).getCurrentStudentId();
        verify(repository).findAll();
    }

    @Test
    void unhappy_getAllLogsForUnknownUserReturnsEmpty() {
        when(userService.getCurrentStudentId()).thenReturn("no-one");
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(logService.getAllLogs().isEmpty());
    }
}
