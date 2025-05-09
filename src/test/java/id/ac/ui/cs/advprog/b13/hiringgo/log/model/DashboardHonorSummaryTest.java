package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardHonorSummaryTest {

    private DashboardHonorSummary dashboardHonorSummary;
    private final int year = 2025;
    private final Month month = Month.MAY;
    private final BigDecimal totalHonor = new BigDecimal("10000000");
    private final long totalHours = 320L;
    private List<DashboardHonor> details;

    @BeforeEach
    void setUp() {
        dashboardHonorSummary = new DashboardHonorSummary();

        DashboardHonor honor1 = new DashboardHonor(
                "VAC-001",
                "Software Engineer",
                year,
                month,
                new BigDecimal("5000000"),
                160L
        );

        DashboardHonor honor2 = new DashboardHonor(
                "VAC-002",
                "Data Scientist",
                year,
                month,
                new BigDecimal("5000000"),
                160L
        );

        details = Arrays.asList(honor1, honor2);
    }

    @Test
    void testEmptyConstructor() {

        assertNotNull(dashboardHonorSummary);
    }

    @Test
    void testParameterizedConstructor() {

        DashboardHonorSummary parameterizedSummary = new DashboardHonorSummary(
                year, month, totalHonor, totalHours, details);

        assertEquals(year, parameterizedSummary.getYear());
        assertEquals(month, parameterizedSummary.getMonth());
        assertEquals(totalHonor, parameterizedSummary.getTotalHonor());
        assertEquals(totalHours, parameterizedSummary.getTotalHours());
        assertEquals(details, parameterizedSummary.getDetails());

        assertEquals(2, parameterizedSummary.getDetails().size());
    }

    @Test
    void testSetAndGetYear() {
        dashboardHonorSummary.setYear(year);
        assertEquals(year, dashboardHonorSummary.getYear());

        int differentYear = 2026;
        dashboardHonorSummary.setYear(differentYear);
        assertEquals(differentYear, dashboardHonorSummary.getYear());
    }

    @Test
    void testSetAndGetMonth() {
        dashboardHonorSummary.setMonth(month);
        assertEquals(month, dashboardHonorSummary.getMonth());

        Month differentMonth = Month.DECEMBER;
        dashboardHonorSummary.setMonth(differentMonth);
        assertEquals(differentMonth, dashboardHonorSummary.getMonth());
    }

    @Test
    void testSetAndGetTotalHonor() {
        dashboardHonorSummary.setTotalHonor(totalHonor);
        assertEquals(totalHonor, dashboardHonorSummary.getTotalHonor());

        BigDecimal differentHonor = new BigDecimal("15000000");
        dashboardHonorSummary.setTotalHonor(differentHonor);
        assertEquals(differentHonor, dashboardHonorSummary.getTotalHonor());
    }

    @Test
    void testSetAndGetTotalHours() {
        dashboardHonorSummary.setTotalHours(totalHours);
        assertEquals(totalHours, dashboardHonorSummary.getTotalHours());
        long differentHours = 400L;
        dashboardHonorSummary.setTotalHours(differentHours);
        assertEquals(differentHours, dashboardHonorSummary.getTotalHours());
    }

    @Test
    void testSetAndGetDetails() {
        dashboardHonorSummary.setDetails(details);
        assertEquals(details, dashboardHonorSummary.getDetails());
        assertEquals(2, dashboardHonorSummary.getDetails().size());

        List<DashboardHonor> emptyList = new ArrayList<>();
        dashboardHonorSummary.setDetails(emptyList);
        assertEquals(emptyList, dashboardHonorSummary.getDetails());
        assertEquals(0, dashboardHonorSummary.getDetails().size());
    }

    @Test
    void testDetailsListModification() {
        dashboardHonorSummary.setDetails(new ArrayList<>(details));

        List<DashboardHonor> modifiableList = dashboardHonorSummary.getDetails();
        DashboardHonor honor3 = new DashboardHonor(
                "VAC-003",
                "UI/UX Designer",
                year,
                month,
                new BigDecimal("4000000"),
                120L
        );
        modifiableList.add(honor3);

        assertEquals(3, dashboardHonorSummary.getDetails().size());
        assertEquals(honor3, dashboardHonorSummary.getDetails().get(2));
    }

    @Test
    void testTotalHonorPrecision() {
        BigDecimal preciseHonor = new BigDecimal("10000000.75");
        dashboardHonorSummary.setTotalHonor(preciseHonor);
        assertEquals(preciseHonor, dashboardHonorSummary.getTotalHonor());
    }

    @Test
    void testSummaryCalculation() {
        DashboardHonorSummary calculatedSummary = new DashboardHonorSummary();
        calculatedSummary.setDetails(details);

        BigDecimal calculatedHonor = BigDecimal.ZERO;
        long calculatedHours = 0L;

        for (DashboardHonor honor : details) {
            calculatedHonor = calculatedHonor.add(honor.getTotalHonor());
            calculatedHours += honor.getTotalHours();
        }

        calculatedSummary.setTotalHonor(calculatedHonor);
        calculatedSummary.setTotalHours(calculatedHours);

        assertEquals(new BigDecimal("10000000"), calculatedSummary.getTotalHonor());
        assertEquals(320L, calculatedSummary.getTotalHours());
    }
}