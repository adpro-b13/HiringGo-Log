package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import java.math.BigDecimal;
import java.time.Month;
import java.util.List;

public class DashboardHonorSummary {
    private int year;
    private Month month;
    private BigDecimal totalHonor;
    private long totalHours;
    private List<DashboardHonor> details;

    public DashboardHonorSummary() {
    }

    public DashboardHonorSummary(int year, Month month, BigDecimal totalHonor, long totalHours, List<DashboardHonor> details) {
        this.year = year;
        this.month = month;
        this.totalHonor = totalHonor;
        this.totalHours = totalHours;
        this.details = details;
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

    public List<DashboardHonor> getDetails() {
        return details;
    }

    public void setDetails(List<DashboardHonor> details) {
        this.details = details;
    }
}