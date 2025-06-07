package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

class DashboardHonorTest {

    private DashboardHonor dashboardHonor;
    private final Long vacancyId = 1L; // Changed from String "VAC-001" to Long
    private final String vacancyTitle = "Software Engineer";
    private final int year = 2025;
    private final Month month = Month.MAY;
    private final BigDecimal totalHonor = new BigDecimal("5000000");
    private final long totalHours = 160L;

    @BeforeEach
    void setUp() {
        dashboardHonor = new DashboardHonor();
    }

    @Test
    void testEmptyConstructor() {
        assertNotNull(dashboardHonor);
    }

    @Test
    void testParameterizedConstructor() {
        DashboardHonor parameterizedDashboard = new DashboardHonor(
                vacancyId, vacancyTitle, year, month, totalHonor, totalHours);

        assertEquals(vacancyId, parameterizedDashboard.getVacancyId());
        assertEquals(vacancyTitle, parameterizedDashboard.getVacancyTitle());
        assertEquals(year, parameterizedDashboard.getYear());
        assertEquals(month, parameterizedDashboard.getMonth());
        assertEquals(totalHonor, parameterizedDashboard.getTotalHonor());
        assertEquals(totalHours, parameterizedDashboard.getTotalHours());
    }

    @Test
    void testSetAndGetVacancyId() {
        dashboardHonor.setVacancyId(vacancyId);
        assertEquals(vacancyId, dashboardHonor.getVacancyId());
    }

    @Test
    void testSetAndGetVacancyTitle() {
        dashboardHonor.setVacancyTitle(vacancyTitle);
        assertEquals(vacancyTitle, dashboardHonor.getVacancyTitle());
    }

    @Test
    void testSetAndGetYear() {
        dashboardHonor.setYear(year);
        assertEquals(year, dashboardHonor.getYear());
    }

    @Test
    void testSetAndGetMonth() {
        dashboardHonor.setMonth(month);
        assertEquals(month, dashboardHonor.getMonth());
    }

    @Test
    void testSetAndGetTotalHonor() {
        dashboardHonor.setTotalHonor(totalHonor);
        assertEquals(totalHonor, dashboardHonor.getTotalHonor());
    }

    @Test
    void testSetAndGetTotalHours() {
        dashboardHonor.setTotalHours(totalHours);
        assertEquals(totalHours, dashboardHonor.getTotalHours());
    }

    @Test
    void testTotalHonorPrecision() {
        BigDecimal preciseHonor = new BigDecimal("5000000.75");
        dashboardHonor.setTotalHonor(preciseHonor);
        assertEquals(preciseHonor, dashboardHonor.getTotalHonor());
    }

    @Test
    void testMonthEnum() {
        dashboardHonor.setMonth(Month.JANUARY);
        assertEquals(Month.JANUARY, dashboardHonor.getMonth());

        dashboardHonor.setMonth(Month.DECEMBER);
        assertEquals(Month.DECEMBER, dashboardHonor.getMonth());
    }
}