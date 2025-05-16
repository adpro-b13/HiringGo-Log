package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.LogStateFactory;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final LogValidator logValidator;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    public LogServiceImpl(LogRepository logRepository, LogValidator logValidator, UserService userService) {
        this.logRepository = logRepository;
        this.logValidator = logValidator;
        this.userService = userService;
    }

    // For Mahasiswa: Create log
    public Log createLog(Log log) {
        logger.info("Attempting to create log for student: {}", log.getStudentId());
        logValidator.validate(log);
        log.setStatus(LogStatus.REPORTED); // Ensure initial status
        Log savedLog = logRepository.save(log);
        logger.info("Log created with ID: {}", savedLog.getId());
        return savedLog;
    }

    // For Mahasiswa: Update log (only if status is REPORTED)
    public Log updateLog(Log log) {
        logger.info("Attempting to update log with ID: {}", log.getId());
        Long id = log.getId();
        Log existing = logRepository.findById(log.getId()).orElse(null);
        boolean isNotExist = id == null || existing == null;
        if (isNotExist) {
            logger.warn("Log not found for update with ID: {}", id);
            throw new IllegalArgumentException("Log not found");
        }
        if (existing.getStatus() != LogStatus.REPORTED) {
            logger.warn("Log with ID: {} cannot be updated due to status: {}", id, existing.getStatus());
            throw new IllegalStateException("Log tidak dapat diubah karena statusnya " + existing.getStatus());
        }
        logValidator.validate(log);
        // Update fields from log to existingLog
        existing.setTitle(log.getTitle());
        existing.setDescription(log.getDescription());
        existing.setCategory(log.getCategory());
        existing.setVacancyId(log.getVacancyId());
        existing.setStartTime(log.getStartTime());
        existing.setEndTime(log.getEndTime());
        existing.setLogDate(log.getLogDate());
        // studentId should generally not be updated, or handled with care
        // status is managed by verifyLog or other specific actions

        Log updatedLog = logRepository.save(existing);
        logger.info("Log updated with ID: {}", updatedLog.getId());
        return updatedLog;
    }

    // For Mahasiswa: Delete log (only if status is REPORTED)
    public void deleteLog(Long id) {
        logger.info("Attempting to delete log with ID: {}", id);
        Log log = logRepository.findById(id).orElseThrow(() -> {
            logger.warn("Log not found for verification with ID: {}", id);
            return new IllegalArgumentException("Log not found");
        });
        if (log == null) {
            logger.warn("Log not found for deletion with ID: {}", id);
            throw new IllegalArgumentException("Log not found");
        }
        if (log.getStatus() != LogStatus.REPORTED) {
            logger.warn("Log with ID: {} cannot be deleted due to status: {}", id, log.getStatus());
            throw new IllegalStateException("Log tidak dapat dihapus karena statusnya " + log.getStatus());
        }
        logRepository.delete(log);
        logger.info("Log deleted with ID: {}", id);
    }

    // For Dosen: Verify log (accept or reject)
    public Log verifyLog(Long id, VerificationAction action) {
        logger.info("Attempting to verify log with ID: {} with action: {}", id, action);
        Log log = logRepository.findById(id).orElseThrow(() -> {
            logger.warn("Log not found for verification with ID: {}", id);
            return new IllegalArgumentException("Log not found");
        });
        if (log == null) {
            logger.warn("Log not found for verification with ID: {}", id);
            throw new IllegalArgumentException("Log not found");
        }
        if (log.getStatus() != LogStatus.REPORTED) {
            logger.warn("Log with ID: {} has already been verified with status: {}", id, log.getStatus());
            throw new IllegalStateException("Log sudah diverifikasi dengan status " + log.getStatus());
        }
        // Use state pattern to determine new status
        LogStatus newStatus = LogStateFactory.getState(log.getStatus()).verify(action);
        log.setStatus(newStatus);
        Log verifiedLog = logRepository.save(log);
        logger.info("Log with ID: {} verified. New status: {}", id, newStatus);
        return verifiedLog;
    }

    @Override
    public List<Log> getAllLogs() {
        String currentStudentId = userService.getCurrentStudentId();
        logger.info("Fetching all logs for student ID: {}", currentStudentId);
        List<Log> logs = logRepository.findAll().stream()
                         .filter(log -> currentStudentId.equals(log.getStudentId()))
                         .collect(Collectors.toList());
        logger.info("Found {} logs for student ID: {}", logs.size(), currentStudentId);
        return logs;
    }
}
