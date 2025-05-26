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
import org.springframework.security.core.Authentication; // Added import
import org.springframework.security.core.context.SecurityContext; // Added import
import org.springframework.security.core.context.SecurityContextHolder; // Added import
import org.junit.jupiter.api.AfterEach; // Added import

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

    // Mocks for Security Context
    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach // Added to clear security context after each test
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unhappy_createLogShouldThrowWhenTitleIsBlank() {
        Log log = new Log("", "Description", "Asistensi", 1L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 10L); // Added Long studentId
        doThrow(new LogValidationException("Judul log tidak boleh kosong.")).when(validator).validate(log);

        LogValidationException exception = assertThrows(LogValidationException.class,
                () -> logService.createLog(log)); // Validation is synchronous
        assertEquals("Judul log tidak boleh kosong.", exception.getMessage());
        verify(validator).validate(log);
        verifyNoInteractions(repository);
    }

    @Test
    void happy_createLogShouldPersistValidLog() {
        Log log = new Log("Valid Log", "Proper log", "Asistensi", 1L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 11L); // Added Long studentId

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
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 12L); // Added Long studentId

        doThrow(new LogValidationException("Id lowongan tidak boleh kosong.")).when(validator).validate(log);

        LogValidationException exception = assertThrows(LogValidationException.class,
                () -> logService.createLog(log)); // Validation is synchronous
        assertEquals("Id lowongan tidak boleh kosong.", exception.getMessage());
        verify(validator).validate(log);
        verifyNoInteractions(repository);
    }

    @Test
    void unhappy_updateLogShouldNotAllowUpdateWhenStatusNotReported() {
        Long id = 6L;
        Log existing = new Log("Log to Update", "Will be verified", "Asistensi", 100L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 13L); // Added Long studentId
        existing.setId(id);
        existing.setStatus(LogStatus.ACCEPTED);

        when(repository.findById(id)).thenReturn(Optional.of(existing));

        Log toUpdate = new Log("Log to Update", "Will be verified", "Asistensi", 100L, 
                existing.getStartTime(), existing.getEndTime(), existing.getLogDate(), 13L); // Added Long studentId
        toUpdate.setId(id);

        // --- Add Security Context Mocking ---
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Assuming the principal is a Long userId as identified from your JwtAuthFilter
        Long mockUserId = toUpdate.getStudentId(); // Use any relevant test user ID
        when(authentication.getPrincipal()).thenReturn(mockUserId);
        // --- End of Security Context Mocking ---

        assertThrows(IllegalStateException.class, () -> logService.updateLog(toUpdate));
        verify(repository).findById(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void happy_updateLogShouldSucceedWhenStatusReported() {
        Long id = 2L;
        Log existing = new Log("Log to Update", "Initial description", "Asistensi", 101L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 14L); // Added Long studentId
        existing.setId(id);
        existing.setStatus(LogStatus.REPORTED);

        // Mock the security context and authentication
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Long mockUserId = existing.getStudentId();
        when(authentication.getPrincipal()).thenReturn(mockUserId); // <<<<<<< RETURNING A LONG

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
        Log existing = new Log("Log to Delete", "Some description", "Asistensi", 102L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 15L); // Added Long studentId
        existing.setId(id);
        existing.setStatus(LogStatus.REPORTED);

        // --- Add Security Context Mocking ---
        when(securityContext.getAuthentication()).thenReturn(authentication);
        // Assuming the principal is a Long userId as identified from your JwtAuthFilter
        Long mockUserId = existing.getStudentId(); // Use any relevant test user ID
        when(authentication.getPrincipal()).thenReturn(mockUserId);
        // --- End of Security Context Mocking ---

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        doNothing().when(repository).delete(existing);

        logService.deleteLog(id);
        verify(repository).findById(id);
        verify(repository).delete(existing);
    }

    @Test
    void happy_verifyLogShouldChangeStatusToAccepted() {
        Long id = 4L;
        Log existing = new Log("Log for Verification", "To be verified", "Asistensi", 103L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), 16L); // Added Long studentId
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
        Log existing = new Log("Log for Rejection", "Needs rejection", "Asistensi", 104L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), 17L); // Added Long studentId
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
        Log existing = new Log("Already Verified", "Should not verify again", "Asistensi", 105L, 
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), 18L); // Added Long studentId
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
        Long studentId = 123L; // Changed to Long
        Long targetVacancyId = 1L; 

        Log log1 = new Log("T1", "D1", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId);
        log1.setId(1L);
        Log log2 = new Log("T2", "D2", "C", 2L, LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), studentId); 
        log2.setId(2L);
        Log log3 = new Log("T3", "D3", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(3), LocalDate.now(), 456L); // Different student (Long)
        log3.setId(3L);

        when(repository.findAll()).thenReturn(List.of(log1, log2, log3));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsStudent(studentId, targetVacancyId); // Pass studentId
        List<Log> result = futureResult.join();

        assertEquals(1, result.size());
        assertEquals(studentId, result.get(0).getStudentId());
        assertEquals(targetVacancyId, result.get(0).getVacancyId());
        assertEquals(log1.getId(), result.get(0).getId());

        // verify(userService).getCurrentStudentId(); // Removed verification
        verify(repository).findAll();
    }

    @Test
    void happy_getAllLogsStudent_returnsEmptyListWhenNoMatchingLogs() {
        Long studentId = 123L; // Changed to Long
        Long targetVacancyId = 999L; 

        Log log1 = new Log("T1", "D1", "C", 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId); 
        log1.setId(1L);

        when(repository.findAll()).thenReturn(List.of(log1)); 

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsStudent(studentId, targetVacancyId); // Pass studentId
        List<Log> result = futureResult.join();

        assertTrue(result.isEmpty());
        // verify(userService).getCurrentStudentId(); // Removed verification
        verify(repository).findAll();
    }

    @Test
    void happy_getAllLogsLecturer_returnsFilteredLogsForLecturerAndReportedStatus() {
        Long targetVacancyId = 1L; 

        Log log1 = new Log("T1", "D1", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 1L); // studentId as Long
        log1.setId(1L);
        log1.setStatus(LogStatus.REPORTED); 

        Log log2 = new Log("T2", "D2", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), 2L); // studentId as Long
        log2.setId(2L);
        log2.setStatus(LogStatus.ACCEPTED); 

        Log log3 = new Log("T3", "D3", "C", 2L, LocalDateTime.now(), LocalDateTime.now().plusHours(3), LocalDate.now(), 3L);  // studentId as Long
        log3.setId(3L);
        log3.setStatus(LogStatus.REPORTED); 

        Log log4 = new Log("T4", "D4", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(4), LocalDate.now(), 4L); // studentId as Long
        log4.setId(4L);
        log4.setStatus(LogStatus.REPORTED); 

        when(repository.findAll()).thenReturn(List.of(log1, log2, log3, log4));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsLecturer(targetVacancyId);
        List<Log> result = futureResult.join();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(log -> log.getVacancyId().equals(targetVacancyId) && log.getStatus() == LogStatus.REPORTED));
        assertTrue(result.contains(log1));
        assertTrue(result.contains(log4));

        verify(repository).findAll();
        // verifyNoInteractions(userService); // userService is removed, so this is no longer needed
    }

    @Test
    void happy_getAllLogsLecturer_returnsEmptyListWhenNoLogsMatchVacancyId() {
        Long targetVacancyId = 999L; 

        Log log1 = new Log("T1", "D1", "C", 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 1L); // studentId as Long
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
        Long targetVacancyId = 1L; 

        Log log1 = new Log("T1", "D1", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), 1L); // studentId as Long
        log1.setId(1L);
        log1.setStatus(LogStatus.ACCEPTED); 

        Log log2 = new Log("T2", "D2", "C", targetVacancyId, LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now(), 2L); // studentId as Long
        log2.setId(2L);
        log2.setStatus(LogStatus.REJECTED); 

        when(repository.findAll()).thenReturn(List.of(log1, log2));

        CompletableFuture<List<Log>> futureResult = logService.getAllLogsLecturer(targetVacancyId);
        List<Log> result = futureResult.join();

        assertTrue(result.isEmpty());
        verify(repository).findAll();
    }


    @Test
    void happy_addMessageToLogShouldSucceedForOwner() {
        Long logId = 1L;
        Long studentId = 123L; // Changed to Long
        String messageContent = "This is a new message.";
        Log existingLog = new Log("Test Log", "Desc", "Cat", 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId); 
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>()); 

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(studentId);

        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));
        when(repository.save(any(Log.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Log updatedLog = logService.addMessageToLog(logId, messageContent);

        assertNotNull(updatedLog);
        assertEquals(1, updatedLog.getMessages().size());
        assertEquals(messageContent, updatedLog.getMessages().get(0));
        // verify(userService).getCurrentStudentId(); // Removed verification
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
        // verifyNoInteractions(userService); // userService is removed
    }

    @Test
    void unhappy_addMessageToLogShouldThrowWhenUserNotOwner() {
        Long logId = 3L;
        Long ownerStudentId = 456L; // Changed to Long
        Long requesterStudentId = 789L; // Changed to Long
        String messageContent = "Unauthorized message attempt.";
        Log existingLog = new Log("Another Log", "Desc", "Cat", 2L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), ownerStudentId); 
        existingLog.setId(logId);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getPrincipal()).thenReturn(requesterStudentId);

        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.addMessageToLog(logId, messageContent));
        assertEquals("User not authorized to add message to this log.", exception.getMessage());
        // verify(userService).getCurrentStudentId(); // Removed verification
        verify(repository).findById(logId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void happy_getMessagesForLogShouldReturnMessagesForOwner() {
        Long logId = 1L;
        Long studentId = 123L; // Changed to Long
        String userRole = "ROLE_MAHASISWA";
        List<String> messages = List.of("Message 1", "Message 2");
        Log existingLog = new Log("Test Log", "Desc", "Cat", 1L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId); 
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>(messages));

        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        List<String> retrievedMessages = logService.getMessagesForLog(logId, studentId, userRole);

        assertNotNull(retrievedMessages);
        assertEquals(2, retrievedMessages.size());
        assertTrue(retrievedMessages.containsAll(messages));
        // verify(userService).getCurrentStudentId(); // Removed verification
        verify(repository).findById(logId);
    }

    @Test
    void unhappy_getMessagesForLogShouldThrowWhenLogNotFound() {
        Long logId = 2L;
        Long requestingUserId = 123L; // Example user
        String userRole = "ROLE_MAHASISWA";
        when(repository.findById(logId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> logService.getMessagesForLog(logId, requestingUserId, userRole));
        assertEquals("Log not found", exception.getMessage());
        verify(repository).findById(logId);
        // verifyNoInteractions(userService); // userService is removed
    }

    @Test
    void unhappy_getMessagesForLogShouldThrowWhenUserNotOwner() {
        Long logId = 3L;
        Long ownerStudentId = 456L; // Changed to Long
        Long requesterStudentId = 789L; // Changed to Long
        String userRole = "ROLE_MAHASISWA";
        Log existingLog = new Log("Another Log", "Desc", "Cat", 2L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), ownerStudentId); 
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>(List.of("Secret message")));

        // SecurityContextHolder mocking is no longer needed here
        // Authentication authentication = mock(Authentication.class);
        // SecurityContext securityContext = mock(SecurityContext.class);
        // when(securityContext.getAuthentication()).thenReturn(authentication);
        // SecurityContextHolder.setContext(securityContext);
        // when(authentication.getPrincipal()).thenReturn(requesterStudentId);

        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> logService.getMessagesForLog(logId, requesterStudentId, userRole));
        assertEquals("User not authorized to view messages for this log.", exception.getMessage());
        // verify(userService).getCurrentStudentId(); // Removed verification
        verify(repository).findById(logId);
    }

    @Test
    void happy_getMessagesForLogShouldReturnEmptyListWhenNoMessages() {
        Long logId = 4L;
        Long studentId = 789L; // Changed to Long
        String userRole = "ROLE_MAHASISWA";
        Log existingLog = new Log("Log With No Messages", "Desc", "Cat", 3L, LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), studentId); 
        existingLog.setId(logId);
        existingLog.setMessages(new ArrayList<>()); 

        // SecurityContextHolder mocking is no longer needed here
        // Authentication authentication = mock(Authentication.class);
        // SecurityContext securityContext = mock(SecurityContext.class);
        // when(securityContext.getAuthentication()).thenReturn(authentication);
        // SecurityContextHolder.setContext(securityContext);
        // when(authentication.getPrincipal()).thenReturn(studentId);

        when(repository.findById(logId)).thenReturn(Optional.of(existingLog));

        List<String> retrievedMessages = logService.getMessagesForLog(logId, studentId, userRole);

        assertNotNull(retrievedMessages);
        assertTrue(retrievedMessages.isEmpty());
        // verify(userService).getCurrentStudentId(); // Removed verification
        verify(repository).findById(logId);
    }
}
