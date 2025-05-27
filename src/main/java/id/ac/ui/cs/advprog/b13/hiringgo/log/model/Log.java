package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId; // Changed from String to Long

    private String title;

    private String description; 

    private String category;

    private Long vacancyId; // Changed from String to Long

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    private LogStatus status = LogStatus.REPORTED;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "log_messages", joinColumns = @JoinColumn(name = "log_id"))
    @Column(name = "message")
    private List<String> messages = new ArrayList<>();

    // Constructors, getters, and setters
    public Log() {
    }

    public Log(String title, String description, String category, Long vacancyId, LocalDateTime startTime, LocalDateTime endTime, LocalDate logDate, Long studentId) { // Changed studentId type
        this.title = title;
        this.description = description;
        this.category = category;
        this.vacancyId = vacancyId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.logDate = logDate;
        this.studentId = studentId;
        // Default status is set by field initializer
    }

    // Constructor without studentId, assuming it might be set differently or not always needed at construction
    public Log(String title, String description, String category, Long vacancyId, LocalDateTime startTime, LocalDateTime endTime, LocalDate logDate) { // Changed vacancyId type
        this.title = title;
        this.description = description;
        this.category = category;
        this.vacancyId = vacancyId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.logDate = logDate;
        // Default status is set by field initializer
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
    
    public Long getVacancyId() { // Changed return type
        return vacancyId;
    }

    public void setVacancyId(Long vacancyId) { // Changed parameter type
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

    public Long getStudentId() { // Changed return type
        return studentId;
    }

    public void setStudentId(Long studentId) { // Changed parameter type
        this.studentId = studentId;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
