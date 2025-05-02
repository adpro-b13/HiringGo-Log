package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Log {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String vacancyId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDate logDate;
    private LogStatus status = LogStatus.REPORTED;

    // Constructors, getters, and setters
    public Log() {
    }

    public Log(String title, String description, String category, String vacancyId, LocalDateTime startTime, LocalDateTime endTime, LocalDate logDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.vacancyId = vacancyId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.logDate = logDate;
        this.status = LogStatus.REPORTED;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getVacancyId() {
        return vacancyId;
    }

    public void setVacancyId(String vacancyId) {
        this.vacancyId = vacancyId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getLogDate() {
        return logDate;
    }

    public void setLogDate(LocalDate logDate) {
        this.logDate = logDate;
    }

    public LogStatus getStatus() {
        return status;
    }

    public void setStatus(LogStatus status) {
        this.status = status;
    }
}
