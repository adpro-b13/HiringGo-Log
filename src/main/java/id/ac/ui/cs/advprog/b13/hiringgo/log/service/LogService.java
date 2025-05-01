package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.LogStateFactory;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogRepository logRepository;
    private final LogValidator logValidator;

    public LogService(LogRepository logRepository, LogValidator logValidator) {
        this.logRepository = logRepository;
        this.logValidator = logValidator;
    }

    // For Mahasiswa: Create log
    public Log createLog(Log log) {
        logValidator.validate(log);
        return logRepository.save(log);
    }

    // For Mahasiswa: Update log (only if status is REPORTED)
    public Log updateLog(Log log) {
        if (log.getId() == null || logRepository.findById(log.getId()) == null) {
            throw new IllegalArgumentException("Log not found");
        }
        Log existing = logRepository.findById(log.getId());
        if (existing.getStatus() != LogStatus.REPORTED) {
            throw new IllegalStateException("Log tidak dapat diubah karena statusnya " + existing.getStatus());
        }
        logValidator.validate(log);
        return logRepository.save(log);
    }

    // For Mahasiswa: Delete log (only if status is REPORTED)
    public void deleteLog(Long id) {
        Log log = logRepository.findById(id);
        if (log == null) {
            throw new IllegalArgumentException("Log not found");
        }
        if (log.getStatus() != LogStatus.REPORTED) {
            throw new IllegalStateException("Log tidak dapat dihapus karena statusnya " + log.getStatus());
        }
        logRepository.delete(log);
    }

    // For Dosen: Verify log (accept or reject)
    public Log verifyLog(Long id, VerificationAction action) {
        Log log = logRepository.findById(id);
        if (log == null) {
            throw new IllegalArgumentException("Log not found");
        }
        if (log.getStatus() != LogStatus.REPORTED) {
            throw new IllegalStateException("Log sudah diverifikasi dengan status " + log.getStatus());
        }
        // Use state pattern to determine new status
        LogStatus newStatus = LogStateFactory.getState(log.getStatus()).verify(action);
        log.setStatus(newStatus);
        return logRepository.save(log);
    }

    // Retrieve all logs
    public java.util.List<Log> getAllLogs() {
        return logRepository.findAll();
    }
}
