package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.LogStateFactory;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;

import java.util.ArrayList;
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
        Log existingLog = logRepository.findById(id).orElseThrow(() -> {
            logger.warn("Log not found for update with ID: {}", id);
            return new IllegalArgumentException("Log not found");
        });

        if (existingLog.getStatus() != LogStatus.REPORTED) {
            logger.warn("Log with ID: {} cannot be updated due to status: {}", id, existingLog.getStatus());
            throw new IllegalStateException("Log tidak dapat diubah karena statusnya " + existingLog.getStatus());
        }

        // Ensure studentId and vacancyId are not changed by setting them from the existing log
        // This makes them effectively immutable for the update operation.
        log.setStudentId(existingLog.getStudentId());
        log.setVacancyId(existingLog.getVacancyId());
        // Also, ensure the status is not changed directly by the input, it's managed by verifyLog
        log.setStatus(existingLog.getStatus());


        logValidator.validate(log); // Validate after ensuring immutable fields are preserved

        // Update mutable fields from log to existingLog
        existingLog.setTitle(log.getTitle());
        existingLog.setDescription(log.getDescription());
        existingLog.setCategory(log.getCategory());
        // vacancyId is set to existingLog's value above, so this effectively re-sets it to the same value if log.getVacancyId() was different.
        // However, to be explicit about not changing it from input:
        // existingLog.setVacancyId(existingLog.getVacancyId()); // This line is redundant due to log.setVacancyId(existingLog.getVacancyId()); above
        existingLog.setStartTime(log.getStartTime());
        existingLog.setEndTime(log.getEndTime());
        existingLog.setLogDate(log.getLogDate());
        // studentId is handled similarly to vacancyId.
        // existingLog.setStudentId(existingLog.getStudentId()); // Redundant

        Log updatedLog = logRepository.save(existingLog);
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

    @Override
    public Log addMessageToLog(Long logId, String messageContent) {
        logger.info("Attempting to add message to log with ID: {}", logId);

        Log log = logRepository.findById(logId).orElseThrow(() -> {
            logger.warn("Log not found for adding message with ID: {}", logId);
            return new IllegalArgumentException("Log not found");
        });

        String currentStudentId = userService.getCurrentStudentId();
        if (!log.getStudentId().equals(currentStudentId)) {
            logger.warn("User {} attempted to add message to log {} owned by {}. Access denied.",
                        currentStudentId, logId, log.getStudentId());
            throw new IllegalStateException("User not authorized to add message to this log.");
        }

        log.getMessages().add(messageContent);
        Log updatedLog = logRepository.save(log);
        logger.info("Message added to log with ID: {}. Total messages: {}", updatedLog.getId(), updatedLog.getMessages().size());
        return updatedLog;
    }

    @Override
    public List<String> getMessagesForLog(Long logId) {
        logger.info("Attempting to fetch messages for log with ID: {}", logId);
        Log log = logRepository.findById(logId).orElseThrow(() -> {
            logger.warn("Log not found for fetching messages with ID: {}", logId);
            return new IllegalArgumentException("Log not found");
        });

        String currentStudentId = userService.getCurrentStudentId();
        // For now, only the owner can see messages. This could be expanded later for other roles (e.g., Dosen).
        if (!log.getStudentId().equals(currentStudentId)) {
            logger.warn("User {} attempted to fetch messages for log {} owned by {}. Access denied.",
                        currentStudentId, logId, log.getStudentId());
            throw new IllegalStateException("User not authorized to view messages for this log.");
        }

        logger.info("Successfully fetched {} messages for log ID: {}", log.getMessages().size(), logId);
        return new ArrayList<>(log.getMessages()); // Return a copy
    }
}
