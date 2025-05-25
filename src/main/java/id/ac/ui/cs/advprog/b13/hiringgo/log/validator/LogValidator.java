package id.ac.ui.cs.advprog.b13.hiringgo.log.validator;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils; // For checking blank strings

@Component
public class LogValidator {

    public void validate(Log log) {
        // Validate studentId
        if (log.getStudentId() == null) { // Changed validation for Long
            throw new LogValidationException("Student ID tidak boleh kosong.");
        }

        // Validate title
        if (!StringUtils.hasText(log.getTitle())) {
            throw new LogValidationException("Judul log tidak boleh kosong.");
        }
        if (log.getTitle() != null && log.getTitle().length() > 255) {
            throw new LogValidationException("Judul log tidak boleh lebih dari 255 karakter.");
        }

        // Validate description
        if (!StringUtils.hasText(log.getDescription())) {
            throw new LogValidationException("Deskripsi log tidak boleh kosong.");
        }
        if (log.getDescription() != null && log.getDescription().length() > 1000) {
            throw new LogValidationException("Deskripsi log tidak boleh lebih dari 1000 karakter.");
        }

        // Validate category
        if (!StringUtils.hasText(log.getCategory())) {
            throw new LogValidationException("Kategori tidak boleh kosong.");
        }

        // Validate vacancyId
        if (log.getVacancyId() == null) { // Changed validation for Long
            throw new LogValidationException("ID lowongan tidak boleh kosong.");
        }

        // Validate startTime
        if (log.getStartTime() == null) {
            throw new LogValidationException("Waktu mulai harus diisi.");
        }

        // Validate endTime
        if (log.getEndTime() == null) {
            throw new LogValidationException("Waktu selesai harus diisi.");
        }

        // Validate logDate
        if (log.getLogDate() == null) {
            throw new LogValidationException("Tanggal log harus diisi.");
        }
        
        // Validate status
        if (log.getStatus() == null) {
            throw new LogValidationException("Status tidak boleh kosong.");
        }

        // Custom business logic validations remain here.
        if (log.getStartTime() != null && log.getEndTime() != null && !log.getStartTime().isBefore(log.getEndTime())) {
            throw new LogValidationException("Waktu mulai harus sebelum waktu selesai.");
        }

        LocalDate today = LocalDate.now();
        if (log.getLogDate() != null && log.getLogDate().isAfter(today)) {
            throw new LogValidationException("Tanggal log tidak boleh di masa depan.");
        }
        
        // Additional validations can be added here (e.g., category specific logic if any)
    }
}

