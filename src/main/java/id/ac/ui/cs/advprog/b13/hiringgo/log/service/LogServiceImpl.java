package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.LogStateFactory;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl {

    private final LogRepository logRepository;
    private final LogValidator logValidator;
    private final UserService userService;

    public LogServiceImpl(LogRepository logRepository, LogValidator logValidator, UserService userService) {
        this.logRepository = logRepository;
        this.logValidator = logValidator;
        this.userService = userService;
    }

    // For Mahasiswa: Create log
    public Log createLog(Log log) {
        logValidator.validate(log);
        return logRepository.save(log);
    }

    // For Mahasiswa: Update log (only if status is REPORTED)
    public Log updateLog(Log log) {

        Long id = log.getId();
        Log existing = logRepository.findById(log.getId());
        boolean isNotExist = id == null || existing == null;
        if (isNotExist) {
            throw new IllegalArgumentException("Log not found");
        }
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

    public List<Log> getAllLogs() {
        String me = userService.getCurrentStudentId();
        return logRepository.findAll().stream()
                         .filter(log -> me.equals(log.getStudentId()))
                         .collect(Collectors.toList());
    }
}
