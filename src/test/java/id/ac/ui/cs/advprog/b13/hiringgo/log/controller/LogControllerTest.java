package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogServiceImpl;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(LogController.class)
class LogControllerTest {

    private MockMvc mockMvc;

    @Autowired // This will be Spring's managed ObjectMapper
    private ObjectMapper objectMapper; // Renamed 'mapper' to 'objectMapper' for consistency, or ensure 'mapper' is used if configured differently.
                                  // For this refactor, we'll use the @Autowired objectMapper.

    @Mock // If using @WebMvcTest, @MockBean is typical for services.
          // If using MockitoExtension standalone, @Mock is fine.
    private LogServiceImpl logService;

    @InjectMocks
    private LogController logController;

    private Log validLog;
    private Log updatedLogData; // Renamed to avoid confusion with a log that is already updated.
    private Log invalidLogByTitle;

    @BeforeEach
    void setUp() {
        // Ensure objectMapper from Spring context has JavaTimeModule (usually default in Spring Boot)
        // If not, configure it:
        // objectMapper.registerModule(new JavaTimeModule());
        // objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(logController).build();

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        validLog = new Log();
        validLog.setId(1L);
        validLog.setStudentId("student123");
        validLog.setTitle("Valid Log Title");
        validLog.setDescription("Valid log description.");
        validLog.setCategory("Internship");
        validLog.setVacancyId("vacancy-001");
        validLog.setStartTime(now.minusHours(1));
        validLog.setEndTime(now);
        validLog.setLogDate(today);
        validLog.setStatus(LogStatus.REPORTED);

        updatedLogData = new Log();
        updatedLogData.setId(1L); // ID should match the log being updated
        updatedLogData.setStudentId("student123Updated");
        updatedLogData.setTitle("Updated Log Title");
        updatedLogData.setDescription("Updated log description.");
        updatedLogData.setCategory("Freelance");
        updatedLogData.setVacancyId("vacancy-002");
        updatedLogData.setStartTime(now.minusHours(2));
        updatedLogData.setEndTime(now.minusHours(1));
        updatedLogData.setLogDate(today.minusDays(1));
        updatedLogData.setStatus(LogStatus.REPORTED); // Status usually remains REPORTED unless verification happens

        invalidLogByTitle = new Log();
        invalidLogByTitle.setId(2L);
        invalidLogByTitle.setStudentId("student456");
        invalidLogByTitle.setTitle(""); // Invalid title (empty)
        invalidLogByTitle.setDescription("Log with invalid title.");
        invalidLogByTitle.setCategory("Competition");
        invalidLogByTitle.setVacancyId("vacancy-003");
        invalidLogByTitle.setStartTime(now.minusHours(3));
        invalidLogByTitle.setEndTime(now.minusHours(2));
        invalidLogByTitle.setLogDate(today);
        invalidLogByTitle.setStatus(LogStatus.REPORTED);
    }

    @Test
    @DisplayName("POST /logs creates a log and returns 201 with Location header")
    void createLog_returnsCreated() throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        Log input = new Log("Test Title","Test Desc","Test Category","VAC-ID-123",
                startTime, startTime.plusHours(1), LocalDate.now(), "student-xyz");
        // Service returns the saved log, potentially with ID and default status
        Log savedLog = new Log(input.getTitle(), input.getDescription(), input.getCategory(), input.getVacancyId(),
                input.getStartTime(), input.getEndTime(), input.getLogDate(), input.getStudentId());
        savedLog.setId(10L);
        savedLog.setStatus(LogStatus.REPORTED); // Default status

        when(logService.createLog(any(Log.class))).thenReturn(savedLog);

        mockMvc.perform(post("/logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/logs/10"))
            .andExpect(jsonPath("$.id").value(10L))
            .andExpect(jsonPath("$.studentId").value("student-xyz"))
            .andExpect(jsonPath("$.status").value("REPORTED"));

        verify(logService).createLog(any(Log.class));
    }

    @Test
    @DisplayName("PUT /logs/{id} updates and returns the log")
    void updateLog_returnsOk() throws Exception {
        Long logIdToUpdate = 5L;
        LocalDateTime startTime = LocalDateTime.now();
        // This is the data sent in the request body for update
        Log requestBodyLog = new Log("New Title","New Desc","Updated Category","VAC-ID-456",
                startTime, startTime.plusHours(2), LocalDate.now(), "student-abc");
        requestBodyLog.setId(logIdToUpdate); // The ID in the body might be ignored or validated against path {id}

        // This is what the service returns after successful update
        Log serviceReturnedLog = new Log(requestBodyLog.getTitle(), requestBodyLog.getDescription(), requestBodyLog.getCategory(), requestBodyLog.getVacancyId(),
                                 requestBodyLog.getStartTime(), requestBodyLog.getEndTime(), requestBodyLog.getLogDate(), requestBodyLog.getStudentId());
        serviceReturnedLog.setId(logIdToUpdate);
        serviceReturnedLog.setStatus(LogStatus.REPORTED); // Assuming status doesn't change on simple update

        // When logService.updateLog is called, it should be with a Log object that has the ID set from the path.
        // The controller sets the ID on the log object from the path variable.
        when(logService.updateLog(any(Log.class))).thenAnswer(invocation -> {
            Log logToUpdate = invocation.getArgument(0);
            // Simulate service returning the updated log
            serviceReturnedLog.setId(logToUpdate.getId()); // Ensure ID matches
            return serviceReturnedLog;
        });


        mockMvc.perform(put("/logs/{id}", logIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBodyLog)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(logIdToUpdate))
            .andExpect(jsonPath("$.title").value("New Title"))
            .andExpect(jsonPath("$.studentId").value("student-abc"));

        verify(logService).updateLog(any(Log.class));
    }

    @Test
    void whenUpdateLog_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Prepare a log object that would fail validation (e.g., empty title)
        // but other required fields are present to isolate the validation error.
        Log logWithEmptyTitle = new Log("", "Valid Description", "Valid Category", "VAC-VALID",
                                        LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), "student-valid");
        logWithEmptyTitle.setId(validLog.getId());


        mockMvc.perform(put("/logs/{id}", validLog.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logWithEmptyTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray()); // Expecting a list of error messages due to @Valid
    }

    @Test
    void whenUpdateLog_serviceThrowsLogValidationException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Service update validation failed";
        when(logService.updateLog(any(Log.class))).thenThrow(new LogValidationException(exceptionMessage));

        mockMvc.perform(put("/logs/{id}", updatedLogData.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLogData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    void whenUpdateLog_serviceThrowsIllegalArgumentException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Log not found for update";
        when(logService.updateLog(any(Log.class))).thenThrow(new IllegalArgumentException(exceptionMessage));

        mockMvc.perform(put("/logs/{id}", updatedLogData.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLogData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    void whenUpdateLog_serviceThrowsIllegalStateException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Log cannot be updated in current state";
        when(logService.updateLog(any(Log.class))).thenThrow(new IllegalStateException(exceptionMessage));

        mockMvc.perform(put("/logs/{id}", updatedLogData.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLogData)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    @DisplayName("DELETE /logs/{id} returns 204")
    void deleteLog_returnsNoContent() throws Exception {
        Long logIdToDelete = 3L;
        doNothing().when(logService).deleteLog(logIdToDelete);

        mockMvc.perform(delete("/logs/{id}", logIdToDelete))
            .andExpect(status().isNoContent());

        verify(logService).deleteLog(logIdToDelete);
    }

    @Test
    @DisplayName("POST /logs/{id}/verify?action=ACCEPT returns updated status")
    void verifyLog_accept() throws Exception {
        Long logIdToVerify = 7L;
        LocalDateTime now = LocalDateTime.now();
        Log verifiedLogFromService = new Log("VerifyTitle","VerifyDesc","VerifyCat","VAC-VERIFY",
                now, now.plusHours(1), LocalDate.now(), "student-verify");
        verifiedLogFromService.setId(logIdToVerify);
        verifiedLogFromService.setStatus(LogStatus.ACCEPTED); // Service sets the status

        when(logService.verifyLog(logIdToVerify, VerificationAction.ACCEPT))
            .thenReturn(verifiedLogFromService);

        mockMvc.perform(post("/logs/{id}/verify", logIdToVerify)
                .param("action", "ACCEPT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(logIdToVerify))
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(logService).verifyLog(logIdToVerify, VerificationAction.ACCEPT);
    }

    @Test
    void whenVerifyLog_withRejectAction_shouldReturnOk() throws Exception {
        Log logToVerify = validLog; // Use a fully formed log from setUp
        Log verifiedLogFromService = new Log(logToVerify.getTitle(), logToVerify.getDescription(), logToVerify.getCategory(), logToVerify.getVacancyId(),
                                     logToVerify.getStartTime(), logToVerify.getEndTime(), logToVerify.getLogDate(), logToVerify.getStudentId());
        verifiedLogFromService.setId(logToVerify.getId());
        verifiedLogFromService.setStatus(LogStatus.REJECTED); 

        when(logService.verifyLog(eq(logToVerify.getId()), eq(VerificationAction.REJECT))).thenReturn(verifiedLogFromService);

        mockMvc.perform(post("/logs/{id}/verify", logToVerify.getId())
                        .param("action", "REJECT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(logToVerify.getId()))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void whenVerifyLog_withInvalidAction_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/logs/{id}/verify", validLog.getId())
                        .param("action", "INVALID_ACTION"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid action value. Must be ACCEPT or REJECT."));
    }

    @Test
    void whenVerifyLog_serviceThrowsIllegalArgumentException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Log not found for verification";
        when(logService.verifyLog(anyLong(), any(VerificationAction.class))).thenThrow(new IllegalArgumentException(exceptionMessage));

        mockMvc.perform(post("/logs/{id}/verify", validLog.getId())
                        .param("action", "ACCEPT"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    void whenVerifyLog_serviceThrowsIllegalStateException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Log cannot be verified in current state";
        when(logService.verifyLog(anyLong(), any(VerificationAction.class))).thenThrow(new IllegalStateException(exceptionMessage));

        mockMvc.perform(post("/logs/{id}/verify", validLog.getId())
                        .param("action", "ACCEPT"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    @DisplayName("GET /logs returns list of logs")
    void getAllLogs_returnsOkWithList() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        Log logA = new Log("Log A","Desc A","Cat A","VAC-A", now.minusDays(1), now.minusDays(1).plusHours(1), today.minusDays(1), "studentA");
        logA.setId(1L);
        logA.setStatus(LogStatus.REPORTED);
        Log logB = new Log("Log B","Desc B","Cat B","VAC-B", now.minusDays(2), now.minusDays(2).plusHours(1), today.minusDays(2), "studentB");
        logB.setId(2L);
        logB.setStatus(LogStatus.ACCEPTED);


        when(logService.getAllLogs()).thenReturn(List.of(logA,logB));

        mockMvc.perform(get("/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Log A"))
            .andExpect(jsonPath("$[1].title").value("Log B"));

        verify(logService).getAllLogs();
    }

    @Test
    void whenGetAllLogs_withNoLogs_shouldReturnEmptyList() throws Exception {
        when(logService.getAllLogs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void whenCreateLog_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Use invalidLogByTitle from setUp which has an empty title
        // Ensure other fields are valid to specifically test title validation.
        // The invalidLogByTitle is already set up with an empty title.

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLogByTitle)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray()); // Expecting a list of error messages
    }

    @Test
    void whenCreateLog_serviceThrowsLogValidationException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Service validation failed";
        // Use validLog from setUp for the request body, the exception is thrown by the service.
        when(logService.createLog(any(Log.class))).thenThrow(new LogValidationException(exceptionMessage));

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLog))) 
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }
}