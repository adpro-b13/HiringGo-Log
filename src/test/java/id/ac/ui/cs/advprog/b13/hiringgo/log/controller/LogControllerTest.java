package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogServiceImpl;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(LogController.class)
class LogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LogServiceImpl logService;

    private Log validLog;
    private Log invalidLog;
    private Log updatedLog;

    @BeforeEach
    void setUp() {

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

        updatedLog = new Log();
        updatedLog.setId(1L);
        updatedLog.setStudentId("student123Updated");
        updatedLog.setTitle("Updated Log Title");
        updatedLog.setDescription("Updated log description.");
        updatedLog.setCategory("Freelance");
        updatedLog.setVacancyId("vacancy-002");
        updatedLog.setStartTime(now.minusHours(2));
        updatedLog.setEndTime(now.minusHours(1));
        updatedLog.setLogDate(today.minusDays(1));
        updatedLog.setStatus(LogStatus.REPORTED);

        invalidLog = new Log();
        invalidLog.setId(2L);
        invalidLog.setStudentId("student456");
        invalidLog.setTitle("");
        invalidLog.setDescription("Log with invalid title.");
        invalidLog.setCategory("Competition");
        invalidLog.setVacancyId("vacancy-003");
        invalidLog.setStartTime(now.minusHours(3));
        invalidLog.setEndTime(now.minusHours(2));
        invalidLog.setLogDate(today);
        invalidLog.setStatus(LogStatus.REPORTED);
    }

    @Test
    @DisplayName("POST /logs creates a log and returns 201 with Location header")
    void createLog_returnsCreated() throws Exception {
        LocalDateTime startTime = LocalDateTime.now();
        Log input = new Log("Test Title","Test Desc","Test Category","VAC-ID-123",
                startTime, startTime.plusHours(1), LocalDate.now(), "student-xyz");
        Log savedLog = new Log(input.getTitle(), input.getDescription(), input.getCategory(), input.getVacancyId(),
                input.getStartTime(), input.getEndTime(), input.getLogDate(), input.getStudentId());
        savedLog.setId(10L);
        savedLog.setStatus(LogStatus.REPORTED);

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
        Log requestBodyLog = new Log("New Title","New Desc","Updated Category","VAC-ID-456",
                startTime, startTime.plusHours(2), LocalDate.now(), "student-abc");
        // ID in requestBodyLog is not strictly necessary as controller sets it from path.
        // requestBodyLog.setId(logIdToUpdate);

        Log serviceReturnedLog = new Log(requestBodyLog.getTitle(), requestBodyLog.getDescription(), requestBodyLog.getCategory(), requestBodyLog.getVacancyId(),
                                 requestBodyLog.getStartTime(), requestBodyLog.getEndTime(), requestBodyLog.getLogDate(), requestBodyLog.getStudentId());
        serviceReturnedLog.setId(logIdToUpdate); // Service returns log with ID.
        serviceReturnedLog.setStatus(LogStatus.REPORTED);

        // Mock the service to return the updated log when called with any Log object
        // that has the correct ID (which the controller ensures by setting log.setId(id)).
        when(logService.updateLog(argThat(log -> log.getId().equals(logIdToUpdate) &&
                                             log.getTitle().equals("New Title")))) // Be more specific if needed
            .thenReturn(serviceReturnedLog);


        mockMvc.perform(put("/logs/{id}", logIdToUpdate)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBodyLog))) // Send log data without ID in body, or with matching ID
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(logIdToUpdate))
            .andExpect(jsonPath("$.title").value("New Title"))
            .andExpect(jsonPath("$.studentId").value("student-abc"));

        verify(logService).updateLog(any(Log.class));
    }

    // @Test
    // void whenUpdateLog_withInvalidData_shouldReturnBadRequest() throws Exception {
    //     Log logWithEmptyTitle = new Log("", "Valid Description", "Valid Category", "VAC-VALID",
    //                                     LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now(), "student-valid");
        
    //     // Stub to prevent NPE if validation doesn't trigger and service is called.
    //     // This doesn't mean the test will pass; it ensures the controller doesn't NPE.
    //     // The test should still fail on status/body if validation isn't working as expected.
    //     when(logService.updateLog(any(Log.class))).thenReturn(new Log()); // Return a dummy non-null Log

    //     mockMvc.perform(put("/logs/{id}", validLog.getId()) 
    //                     .contentType(MediaType.APPLICATION_JSON)
    //                     .content(objectMapper.writeValueAsString(logWithEmptyTitle)))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$").isArray());
    // }

    @Test
    void whenUpdateLog_serviceThrowsLogValidationException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Service update validation failed";
        when(logService.updateLog(any(Log.class))).thenThrow(new LogValidationException(exceptionMessage));

        mockMvc.perform(put("/logs/{id}", updatedLog.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLog)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    void whenUpdateLog_serviceThrowsIllegalArgumentException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Log not found for update";
        when(logService.updateLog(any(Log.class))).thenThrow(new IllegalArgumentException(exceptionMessage));

        mockMvc.perform(put("/logs/{id}", updatedLog.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLog)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    void whenUpdateLog_serviceThrowsIllegalStateException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Log cannot be updated in current state";
        when(logService.updateLog(any(Log.class))).thenThrow(new IllegalStateException(exceptionMessage));

        mockMvc.perform(put("/logs/{id}", updatedLog.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLog)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }

    @Test
    @DisplayName("DELETE /logs/{id} returns 204")
    void deleteLog_returnsNoContent() throws Exception {
        doNothing().when(logService).deleteLog(3L);

        mockMvc.perform(delete("/logs/3"))
            .andExpect(status().isNoContent());

        verify(logService).deleteLog(3L);
    }

    @Test
    @DisplayName("POST /logs/{id}/verify?action=ACCEPT returns updated status")
    void verifyLog_accept() throws Exception {
        Log verified = new Log("T","D","C","VAC-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        verified.setId(7L);
        verified.setStatus(LogStatus.ACCEPTED); // Assuming LogStatus is an enum

        when(logService.verifyLog(7L, VerificationAction.ACCEPT))
            .thenReturn(verified);

        mockMvc.perform(post("/logs/7/verify")
                .param("action", "ACCEPT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(logService).verifyLog(7L, VerificationAction.ACCEPT);
    }

    @Test
    void whenVerifyLog_withRejectAction_shouldReturnOk() throws Exception {
        Log verifiedLog = new Log();
        verifiedLog.setId(validLog.getId());
        verifiedLog.setStatus(LogStatus.REJECTED); // Set the enum value directly
        when(logService.verifyLog(eq(validLog.getId()), eq(VerificationAction.REJECT))).thenReturn(verifiedLog);

        mockMvc.perform(post("/logs/{id}/verify", validLog.getId())
                        .param("action", "REJECT"))
                .andExpect(status().isOk())
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
        Log a = new Log("A","D","C","VAC-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        a.setId(1L);
        Log b = new Log("B","D2","C","VAC-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        b.setId(2L);

        when(logService.getAllLogs()).thenReturn(List.of(a,b));

        mockMvc.perform(get("/logs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        verify(logService).getAllLogs();
    }

    @Test
    void whenGetAllLogs_withNoLogs_shouldReturnEmptyList() throws Exception {
        when(logService.getAllLogs()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

//    @Test
//    void whenCreateLog_withInvalidData_shouldReturnBadRequest() throws Exception {
//        // 'invalidLog' is set up in @BeforeEach to be invalid (e.g., missing title)
//        // Assumes Log model has validation annotations (e.g., @NotBlank on title)
//
//        // Stub to prevent NPE if validation doesn't trigger and service is called.
//        // This doesn't mean the test will pass; it ensures the controller doesn't NPE.
//        // The test should still fail on status/body if validation isn't working as expected.
//        when(logService.createLog(any(Log.class))).thenReturn(new Log()); // Return a dummy non-null Log
//
//        mockMvc.perform(post("/logs")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(invalidLog)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$").isArray()); // Expecting a list of error messages
//    }

    @Test
    void whenCreateLog_serviceThrowsLogValidationException_shouldReturnBadRequest() throws Exception {
        String exceptionMessage = "Service validation failed";
        // Use validLog for the payload, as the service itself is throwing the exception
        when(logService.createLog(any(Log.class))).thenThrow(new LogValidationException(exceptionMessage));

        mockMvc.perform(post("/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLog)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(exceptionMessage));
    }
}