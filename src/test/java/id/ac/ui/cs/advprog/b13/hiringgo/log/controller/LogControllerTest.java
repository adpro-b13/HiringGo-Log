package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LogControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private LogServiceImpl logService;

    @InjectMocks
    private LogController logController;

    @BeforeEach
    void setUp() {
        // Register JavaTimeModule for LocalDateTime support
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(logController).build();
    }

    @Test
    @DisplayName("POST /logs creates a log and returns 201 with Location header")
    void createLog_returnsCreated() throws Exception {
        Log input = new Log("T","D","C","VAC-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        Log saved = new Log(input.getTitle(), input.getDescription(), input.getCategory(), input.getVacancyId(),
                input.getStartTime(), input.getEndTime(), input.getLogDate());
        saved.setId(10L);
        saved.setStatus(LogStatus.REPORTED);

        when(logService.createLog(any(Log.class))).thenReturn(saved);

        mockMvc.perform(post("/logs")
                .contentType("application/json")
                .content(mapper.writeValueAsString(input)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "/logs/10"))
            .andExpect(jsonPath("$.id").value(10))
            .andExpect(jsonPath("$.status").value("REPORTED"));

        verify(logService).createLog(any(Log.class));
    }

    @Test
    @DisplayName("PUT /logs/{id} updates and returns the log")
    void updateLog_returnsOk() throws Exception {
        Log updated = new Log("New","NewDesc","C","VAC-2",
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), LocalDate.now());
        updated.setId(5L);
        when(logService.updateLog(any(Log.class))).thenReturn(updated);

        mockMvc.perform(put("/logs/5")
                .contentType("application/json")
                .content(mapper.writeValueAsString(updated)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.title").value("New"));

        verify(logService).updateLog(any(Log.class));
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
        verified.setStatus(LogStatus.ACCEPTED);

        when(logService.verifyLog(7L, VerificationAction.ACCEPT))
            .thenReturn(verified);

        mockMvc.perform(post("/logs/7/verify")
                .param("action", "ACCEPT"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ACCEPTED"));

        verify(logService).verifyLog(7L, VerificationAction.ACCEPT);
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
}