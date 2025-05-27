package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import java.math.BigDecimal;
import java.time.Month;

public class DashboardHonor {
    private Long vacancyId; // Changed from String to Long
    private String vacancyTitle;
    private int year;
    private Month month;
    private BigDecimal totalHonor;
    private long totalHours;

    public DashboardHonor() {
    }

    public DashboardHonor(Long vacancyId, String vacancyTitle, int year, Month month, BigDecimal totalHonor, long totalHours) { // Changed parameter type
        this.vacancyId = vacancyId;
        this.vacancyTitle = vacancyTitle;
        this.year = year;
        this.month = month;
        this.totalHonor = totalHonor;
        this.totalHours = totalHours;
    }

    public Long getVacancyId() { // Changed return type
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) { // Changed parameter type
        this.vacancyId = vacancyId;
    }

    public String getVacancyTitle() {
        return vacancyTitle;
    }

    public void setVacancyTitle(String vacancyTitle) {
        this.vacancyTitle = vacancyTitle;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public Month getMonth() {
        return month;
    }

    public void setMonth(Month month) {
        this.month = month;
    }

    public BigDecimal getTotalHonor() {
        return totalHonor;
    }

    public void setTotalHonor(BigDecimal totalHonor) {
        this.totalHonor = totalHonor;
    }

    public long getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(long totalHours) {
        this.totalHours = totalHours;
    }
}