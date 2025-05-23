package id.ac.ui.cs.advprog.b13.hiringgo.log.validator;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class LogValidator {

    public void validate(Log log) {
        // Basic null/empty checks for title, vacancyId, startTime, endTime, logDate
        // are now handled by @NotBlank and @NotNull annotations on the Log model.
        // studentId and category are also handled by @NotBlank.

        // Custom business logic validations remain here.
        if (log.getStartTime() != null && log.getEndTime() != null && !log.getStartTime().isBefore(log.getEndTime())) {
            throw new LogValidationException("Waktu mulai harus sebelum waktu selesai.");
        }

        LocalDate today = LocalDate.now();
        if (log.getLogDate() != null && log.getLogDate().isAfter(today)) {
            throw new LogValidationException("Tanggal log tidak boleh di masa depan.");
        }

        if (log.getStartTime() != null && log.getEndTime() != null) {
            long durationInHours = Duration.between(log.getStartTime(), log.getEndTime()).toHours();
            if (durationInHours > 12) {
                throw new LogValidationException("Durasi log tidak boleh lebih dari 12 jam.");
            }
        }
        
        // Additional validations can be added here (e.g., category specific logic if any)
    }
}

