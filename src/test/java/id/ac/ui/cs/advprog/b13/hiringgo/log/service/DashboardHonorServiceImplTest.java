package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonor;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonorSummary;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardHonorServiceImplTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private UserService userService;

    private DashboardHonorServiceImpl dashboardHonorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        dashboardHonorService = new DashboardHonorServiceImpl(logRepository, userService);
    }

    @Test
    void getDashboardHonor_shouldReturnCorrectHonorForAcceptedLogs() {
        // Setup
        Long studentId = 123L; // Changed from String to Long
        Long vacancyId = 456L; // Changed from String to Long
        int year = 2025;
        int month = 5;

        Log log1 = createLog(studentId, vacancyId, "2025-05-01", "09:00", "11:00", LogStatus.ACCEPTED);
        Log log2 = createLog(studentId, vacancyId, "2025-05-02", "13:00", "15:00", LogStatus.ACCEPTED);
        Log log3 = createLog(studentId, vacancyId, "2025-05-03", "14:00", "15:30", LogStatus.REJECTED); // Should be excluded
        Log log4 = createLog(studentId, 789L, "2025-05-04", "10:00", "12:00", LogStatus.ACCEPTED); // Different vacancy
        Log log5 = createLog(studentId, vacancyId, "2025-06-01", "09:00", "11:00", LogStatus.ACCEPTED); // Different month

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(logRepository.findAll()).thenReturn(Arrays.asList(log1, log2, log3, log4, log5));

        List<DashboardHonor> result = dashboardHonorService.getDashboardHonor(year, month);

        assertEquals(2, result.size());

        DashboardHonor vacancy456Honor = result.stream()
                .filter(honor -> vacancyId.equals(honor.getVacancyId())) // Now comparing Long with Long
                .findFirst()
                .orElse(null);

        assertNotNull(vacancy456Honor);
        assertEquals(year, vacancy456Honor.getYear());
        assertEquals(Month.of(month), vacancy456Honor.getMonth());
        assertEquals(4, vacancy456Honor.getTotalHours());

        // Expected honor: 4 hours * 27,500 = 110,000
        assertEquals(0, new BigDecimal("110000.00").compareTo(vacancy456Honor.getTotalHonor()));

        DashboardHonor vacancy789Honor = result.stream()
                .filter(honor -> Long.valueOf(789L).equals(honor.getVacancyId()))
                .findFirst()
                .orElse(null);

        assertNotNull(vacancy789Honor);
        assertEquals(2, vacancy789Honor.getTotalHours());

        // Expected honor: 2 hours * 27,500 = 55,000
        assertEquals(0, new BigDecimal("55000.00").compareTo(vacancy789Honor.getTotalHonor()));
    }

    @Test
    void getDashboardHonorSummary_shouldReturnCorrectSummary() {
        // Setup
        Long studentId = 123L; // Changed from String to Long
        int year = 2025;
        int month = 5;

        Log log1 = createLog(studentId, 456L, "2025-05-01", "09:00", "11:00", LogStatus.ACCEPTED);
        Log log2 = createLog(studentId, 789L, "2025-05-02", "13:00", "15:00", LogStatus.ACCEPTED);

        when(userService.getCurrentStudentId()).thenReturn(studentId);
        when(logRepository.findAll()).thenReturn(Arrays.asList(log1, log2));

        DashboardHonorSummary result = dashboardHonorService.getDashboardHonorSummary(year, month);

        assertNotNull(result);
        assertEquals(year, result.getYear());
        assertEquals(Month.of(month), result.getMonth());
        assertEquals(4, result.getTotalHours());

        // Expected total honor: 4 hours * 27,500 = 110,000
        assertEquals(0, new BigDecimal("110000.00").compareTo(result.getTotalHonor()));
        assertEquals(2, result.getDetails().size());
    }

    private Log createLog(Long studentId, Long vacancyId, String date, String startTime, String endTime, LogStatus status) { // Changed parameter types
        LocalDate logDate = LocalDate.parse(date);
        LocalDateTime start = LocalDateTime.parse(date + "T" + startTime + ":00");
        LocalDateTime end = LocalDateTime.parse(date + "T" + endTime + ":00");

        Log log = new Log();
        log.setStudentId(studentId); // Now setting Long
        log.setVacancyId(vacancyId); // Now setting Long
        log.setTitle("Test Log");
        log.setDescription("Test Description");
        log.setCategory("Asistensi");
        log.setLogDate(logDate);
        log.setStartTime(start);
        log.setEndTime(end);
        log.setStatus(status);
        return log;
    }
}