package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonor;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonorSummary;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.DashboardHonorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardHonorControllerTest {

    @Mock
    private DashboardHonorService dashboardHonorService;

    private DashboardHonorController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new DashboardHonorController(dashboardHonorService);
    }

    @Test
    void getDashboardHonorDetails_shouldReturnCorrectData() {
        // Setup
        int year = 2025;
        int month = 5;

        DashboardHonor honor1 = new DashboardHonor("vacancy123", "Vacancy Title 1", year, Month.of(month),
                new BigDecimal("110000"), 4);
        DashboardHonor honor2 = new DashboardHonor("vacancy456", "Vacancy Title 2", year, Month.of(month),
                new BigDecimal("55000"), 2);

        List<DashboardHonor> expectedHonors = Arrays.asList(honor1, honor2);

        when(dashboardHonorService.getDashboardHonor(year, month)).thenReturn(expectedHonors);

        ResponseEntity<List<DashboardHonor>> response = controller.getDashboardHonorDetails(year, month);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedHonors, response.getBody());
        verify(dashboardHonorService).getDashboardHonor(year, month);
    }

    @Test
    void getDashboardHonorSummary_shouldReturnCorrectData() {

        int year = 2025;
        int month = 5;

        DashboardHonor honor1 = new DashboardHonor("vacancy123", "Vacancy Title 1", year, Month.of(month),
                new BigDecimal("110000"), 4);
        DashboardHonor honor2 = new DashboardHonor("vacancy456", "Vacancy Title 2", year, Month.of(month),
                new BigDecimal("55000"), 2);

        List<DashboardHonor> details = Arrays.asList(honor1, honor2);
        DashboardHonorSummary expectedSummary = new DashboardHonorSummary(
                year, Month.of(month), new BigDecimal("165000"), 6, details);

        when(dashboardHonorService.getDashboardHonorSummary(year, month)).thenReturn(expectedSummary);

        ResponseEntity<DashboardHonorSummary> response = controller.getDashboardHonorSummary(year, month);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSummary, response.getBody());
        verify(dashboardHonorService).getDashboardHonorSummary(year, month);
    }

    @Test
    void getDashboardHonorDetails_withInvalidMonth_shouldReturnBadRequest() {

        ResponseEntity<List<DashboardHonor>> response = controller.getDashboardHonorDetails(2025, 13);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(dashboardHonorService, never()).getDashboardHonor(anyInt(), anyInt());
    }

    @Test
    void getDashboardHonorSummary_withInvalidMonth_shouldReturnBadRequest() {

        ResponseEntity<DashboardHonorSummary> response = controller.getDashboardHonorSummary(2025, 0);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(dashboardHonorService, never()).getDashboardHonorSummary(anyInt(), anyInt());
    }
}