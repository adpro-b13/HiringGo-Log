package id.ac.ui.cs.advprog.b13.hiringgo.log.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "logs")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Student ID tidak boleh kosong.")
    private String studentId;

    @NotBlank(message = "Judul log tidak boleh kosong.")
    @Size(max = 255, message = "Judul log tidak boleh lebih dari 255 karakter.")
    private String title;

    @NotBlank(message = "Deskripsi log tidak boleh kosong.")
    @Size(max = 1000, message = "Deskripsi log tidak boleh lebih dari 1000 karakter.")
    private String description; 

    @NotBlank(message = "Kategori tidak boleh kosong.")
    private String category;

    @NotBlank(message = "ID lowongan tidak boleh kosong.")
    private String vacancyId;

    @NotNull(message = "Waktu mulai harus diisi.")
    private LocalDateTime startTime;

    @NotNull(message = "Waktu selesai harus diisi.")
    private LocalDateTime endTime;

    @NotNull(message = "Tanggal log harus diisi.")
    private LocalDate logDate;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status tidak boleh kosong.") // Added message for consistency, though default helps
    private LogStatus status = LogStatus.REPORTED;

    // Constructors, getters, and setters
    public Log() {
    }

    public Log(String title, String description, String category, String vacancyId, LocalDateTime startTime, LocalDateTime endTime, LocalDate logDate, String studentId) {
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
    public Log(String title, String description, String category, String vacancyId, LocalDateTime startTime, LocalDateTime endTime, LocalDate logDate) {
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

    public String getStudentId() {
        return studentId;
    }
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
