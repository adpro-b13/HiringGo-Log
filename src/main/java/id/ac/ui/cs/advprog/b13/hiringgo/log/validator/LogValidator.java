package id.ac.ui.cs.advprog.b13.hiringgo.log.validator;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class LogValidator {

    public void validate(Log log) {
        if (log.getTitle() == null || log.getTitle().trim().isEmpty()) {
            throw new LogValidationException("Judul log tidak boleh kosong.");
        }
        if (log.getStartTime() == null || log.getEndTime() == null) {
            throw new LogValidationException("Waktu mulai dan selesai harus diisi.");
        }
        if (!log.getStartTime().isBefore(log.getEndTime())) {
            throw new LogValidationException("Waktu mulai harus sebelum waktu selesai.");
        }
        LocalDate today = LocalDate.now();
        if (log.getLogDate() == null || log.getLogDate().isAfter(today)) {
            throw new LogValidationException("Tanggal log tidak boleh di masa depan.");
        }
        long durationInHours = Duration.between(log.getStartTime(), log.getEndTime()).toHours();
        if (durationInHours > 12) {
            throw new LogValidationException("Durasi log tidak boleh lebih dari 12 jam.");
        }
        // Additional validations can be added here (e.g., category validation)
    }
}

