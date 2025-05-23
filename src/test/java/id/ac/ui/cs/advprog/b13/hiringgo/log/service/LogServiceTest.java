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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

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
                () -> logService.createLog(log)); // Validation is synchronous
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

        CompletableFuture<Log> futureResult = logService.createLog(log);
        Log result = futureResult.join(); // or .get()
        
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
                () -> logService.createLog(log)); // Validation is synchronous
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

        when(repository.findById(id)).thenReturn(Optional.of(existing));

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

        when(repository.findById(id)).thenReturn(Optional.of(existing));
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

        when(repository.findById(id)).thenReturn(Optional.of(existing));
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

        when(repository.findById(id)).thenReturn(Optional.of(existing));
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

        when(repository.findById(id)).thenReturn(Optional.of(existing));
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

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.verifyLog(id, VerificationAction.REJECT));
        assertTrue(exception.getMessage().contains("Log sudah diverifikasi"));
        verify(repository).findById(id);
    }

    @Test
    void happy_getAllLogsStudent_returnsFilteredLogsForStudentAndVacancy() {
        String studentId = "user-123";
        String targetVacancyId = "VAC-001";

        Log log1 = new Log("T1", "D1", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId);
        log1.setId(1L);
        Log log2 = new Log("T2", "D2", "C", "VAC-002", LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), studentId); // Different vacancy
        log2.setId(2L);
        Log log3 = new Log("T3", "D3", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(3), LocalDate.now(), "other-user-456"); // Different student
        log3.setId(3L);

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(repository.findAll()).thenReturn(List.of(log1, log2, log3));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsStudent(targetVacancyId);
        List<Log> result = futureResult.join();

        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getStudentId());
        assertEquals(targetVacancyId, result.get(0).getVacancyId());
        assertEquals(log1.getId(), result.get(0).getId());

        verify(userService).getCurrentStudentId();
        verify(repository).findAll();
    }

    @Test
    void happy_getAllLogsStudent_returnsEmptyListWhenNoMatchingLogs() {
        String studentId = "user-123";
        String targetVacancyId = "VAC-NONEXISTENT";

        Log log1 = new Log("T1", "D1", "C", "VAC-001", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId);
        log1.setId(1L);

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(repository.findAll()).thenReturn(List.of(log1)); // Repository has logs, but not for targetVacancyId

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsStudent(targetVacancyId);
        List<Log> result = futureResult.join();

        assertTrue(result.isEmpty());
        verify(userService).getCurrentStudentId();
        verify(repository).findAll();
    }

    @Test
    void happy_getAllLogsLecturer_returnsFilteredLogsForLecturerAndReportedStatus() {
        String targetVacancyId = "VAC-001";

        Log log1 = new Log("T1", "D1", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), "student1");
        log1.setId(1L);
        log1.setStatus(LogStatus.REPORTED); // Match

        Log log2 = new Log("T2", "D2", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), "student2");
        log2.setId(2L);
        log2.setStatus(LogStatus.ACCEPTED); // Different status

        Log log3 = new Log("T3", "D3", "C", "VAC-002", LocalDateTime.now(), LocalDateTime.now().plusHours(3), LocalDate.now(), "student3");
        log3.setId(3L);
        log3.setStatus(LogStatus.REPORTED); // Different vacancy

        Log log4 = new Log("T4", "D4", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(4), LocalDate.now(), "student4");
        log4.setId(4L);
        log4.setStatus(LogStatus.REPORTED); // Match

        when(repository.findAll()).thenReturn(List.of(log1, log2, log3, log4));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsLecturer(targetVacancyId);
        List<Log> result = futureResult.join();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> log.getVacancyId().equals(targetVacancyId) && log.getStatus() == LogStatus.REPORTED));
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log4));

        verify(repository).findAll();
        verifyNoInteractions(userService); // userService should not be called for lecturer logs
    }

    @Test
    void happy_getAllLogsLecturer_returnsEmptyListWhenNoLogsMatchVacancyId() {
        String targetVacancyId = "VAC-NONEXISTENT";

        Log log1 = new Log("T1", "D1", "C", "VAC-001", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), "student1");
        log1.setId(1L);
        log1.setStatus(LogStatus.REPORTED);

        when(repository.findAll()).thenReturn(List.of(log1));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsLecturer(targetVacancyId);
        List<Log> result = futureResult.join();

        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void happy_getAllLogsLecturer_returnsEmptyListWhenLogsExistForVacancyButNotReportedStatus() {
        String targetVacancyId = "VAC-001";

        Log log1 = new Log("T1", "D1", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), "student1");
        log1.setId(1L);
        log1.setStatus(LogStatus.ACCEPTED); // Not REPORTED

        Log log2 = new Log("T2", "D2", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), "student2");
        log2.setId(2L);
        log2.setStatus(LogStatus.REJECTED); // Not REPORTED

        when(repository.findAll()).thenReturn(List.of(log1, log2));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsLecturer(targetVacancyId);
        List<Log> result = futureResult.join();

        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }


    @Test
    void happy_addMessageToLogShouldSucceedForOwner() {
        Long logId = 1L;
        String studentId = "user-123";
        String messageContent = "This is a new message.";
        Log existingLog = new Log("Test Log", "Desc", "Cat", "VAC-1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId);
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>()); // Initialize messages list

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(repository.save(any(Log.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Log updatedLog = logService.addMessageToLog(logId, messageContent);

        assertNotNull(updatedLog);
        assertEquals(1, updatedLog.getMessages().size());
        assertEquals(messageContent, updatedLog.getMessages().get(0));
        verify(userService).getCurrentStudentId();
        verify(repository).findById(logId);
        verify(repository).save(existingLog);
    }

    @Test
    void unhappy_addMessageToLogShouldThrowWhenLogNotFound() {
        Long logId = 2L;
        String messageContent = "This message won't be added.";
        when(repository.findById(logId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logService.addMessageToLog(logId, messageContent));
        assertEquals("Log not found", exception.getMessage());
        verify(repository).findById(logId);
        verifyNoMoreInteractions(repository);
        verifyNoInteractions(userService);
    }

    @Test
    void unhappy_addMessageToLogShouldThrowWhenUserNotOwner() {
        Long logId = 3L;
        String ownerStudentId = "owner-456";
        String requesterStudentId = "requester-789";
        String messageContent = "Unauthorized message attempt.";
        Log existingLog = new Log("Another Log", "Desc", "Cat", "VAC-2", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), ownerStudentId);
        existingLog.setId(logId);

        when(userService.getCurrentStudentId()).thenReturn(requesterStudentId);
        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.addMessageToLog(logId, messageContent));
        assertEquals("User not authorized to add message to this log.", exception.getMessage());
        verify(userService).getCurrentStudentId();
        verify(repository).findById(logId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void happy_getMessagesForLogShouldReturnMessagesForOwner() {
        Long logId = 1L;
        String studentId = "user-123";
        List<String> messages = List.of("Message 1", "Message 2");
        Log existingLog = new Log("Test Log", "Desc", "Cat", "VAC-1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId);
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>(messages));

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        List<String> retrievedMessages = logService.getMessagesForLog(logId);

        assertNotNull(retrievedMessages);
        assertEquals(2, retrievedMessages.size());
        assertTrue(retrievedMessages.containsAll(messages));
        verify(userService).getCurrentStudentId();
        verify(repository).findById(logId);
    }

    @Test
    void unhappy_getMessagesForLogShouldThrowWhenLogNotFound() {
        Long logId = 2L;
        when(repository.findById(logId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logService.getMessagesForLog(logId));
        assertEquals("Log not found", exception.getMessage());
        verify(repository).findById(logId);
        verifyNoInteractions(userService);
    }

    @Test
    void unhappy_getMessagesForLogShouldThrowWhenUserNotOwner() {
        Long logId = 3L;
        String ownerStudentId = "owner-456";
        String requesterStudentId = "requester-789";
        Log existingLog = new Log("Another Log", "Desc", "Cat", "VAC-2", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), ownerStudentId);
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>(List.of("Secret message")));

        when(userService.getCurrentStudentId()).thenReturn(requesterStudentId);
        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.getMessagesForLog(logId));
        assertEquals("User not authorized to view messages for this log.", exception.getMessage());
        verify(userService).getCurrentStudentId();
        verify(repository).findById(logId);
    }

    @Test
    void happy_getMessagesForLogShouldReturnEmptyListWhenNoMessages() {
        Long logId = 4L;
        String studentId = "user-789";
        Log existingLog = new Log("Log With No Messages", "Desc", "Cat", "VAC-3", LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId);
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>()); // Explicitly empty

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        List<String> retrievedMessages = logService.getMessagesForLog(logId);

        assertNotNull(retrievedMessages);
        assertTrue(retrievedMessages.isEmpty());
        verify(userService).getCurrentStudentId();
        verify(repository).findById(logId);
    }
}
