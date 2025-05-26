package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.LogStateFactory;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder; // Added import
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LogServiceImpl implements LogService {

    private final LogRepository logRepository;
    private final LogValidator logValidator;
    // private final UserService userService; // Removed UserService
    private static final Logger logger = LoggerFactory.getLogger(LogServiceImpl.class);

    public LogServiceImpl(LogRepository logRepository, LogValidator logValidator) { // Removed UserService from constructor
        this.logRepository = logRepository;
        this.logValidator = logValidator;
        // this.userService = userService; // Removed assignment
    }

    // For Mahasiswa: Create log
    @Override
    public CompletableFuture<Log> createLog(Log log) {
        // Run validation and setup synchronously in calling thread
        logValidator.validate(log);
        log.setStatus(LogStatus.REPORTED);
        return asyncCreate(log); // now safe
    }

    @Async
    public CompletableFuture<Log> asyncCreate(Log log) {
        Log savedLog = logRepository.save(log);
        logger.info("Log created with ID: {}", savedLog.getId());
        return CompletableFuture.completedFuture(savedLog);
    }


    // For Mahasiswa: Update log (only if status is REPORTED)
    @Override
    public Log updateLog(Log log) {
        logger.info("Attempting to update log with ID: {}", log.getId());
        Long id = log.getId();
        Log existingLog = logRepository.findById(id).orElseThrow(() -> {
            logger.warn("Log not found for update with ID: {}", id);
            return new IllegalArgumentException("Log not found");
        });
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!existingLog.getStudentId().equals(userId)) {
            logger.warn("User {} attempted to update log {} owned by {}. Access denied.", userId, id, existingLog.getStudentId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to update this log.");
        }



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
    @Override
    public void deleteLog(Long id) {
        logger.info("Attempting to delete log with ID: {}", id);

        Log log = logRepository.findById(id).orElseThrow(() -> {
            logger.warn("Log not found for deletion with ID: {}", id);
            return new IllegalArgumentException("Log not found");
        });

        // 🔒 Check if the current user is the owner
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long)) {
            logger.error("Principal is not of type Long. Actual: {}", principal.getClass().getName());
            throw new IllegalStateException("User principal is not of the expected type (Long).");
        }
        Long currentUserId = (Long) principal;

        if (!log.getStudentId().equals(currentUserId)) {
            logger.warn("User {} attempted to delete log {} owned by {}. Access denied.",
                    currentUserId, id, log.getStudentId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to delete this log.");

        }

        if (log.getStatus() != LogStatus.REPORTED) {
            logger.warn("Log with ID: {} cannot be deleted due to status: {}", id, log.getStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Log tidak dapat dihapus karena statusnya " + log.getStatus());
        }

        logRepository.delete(log);
        logger.info("Log deleted with ID: {}", id);
    }


    // For Dosen: Verify log (accept or reject)
    @Override
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

    @Async
    @Override
    public CompletableFuture<List<Log>> getAllLogsStudent(Long studentId, Long vacancyId) { // Changed String to Long
        // Assuming vacancyId is validated (not null) by the controller via @RequestParam
        // and potentially other validation annotations.
        // studentId is now passed as a parameter.
        return CompletableFuture.supplyAsync(() -> {
            // Log assuming vacancyId is non-null as per controller validation.
            logger.info("Fetching logs for student ID: {} and specific vacancy ID: {}", studentId, vacancyId);
            List<Log> logs = logRepository.findAll().stream()
                    .filter(logObject -> studentId.equals(logObject.getStudentId()))
                    .filter(logObject -> vacancyId.equals(logObject.getVacancyId())) // Filter by the specific vacancyId
                    .collect(Collectors.toList());
            logger.info("Found {} logs for student ID: {} and vacancy ID: {}", logs.size(), studentId, vacancyId);
            return logs;
        });
    }

    @Async
    @Override
    public CompletableFuture<List<Log>> getAllLogsLecturer(Long vacancyId) { // Changed String to Long
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching logs for lecturer for vacancy ID: {} with status REPORTED", vacancyId);
            List<Log> logs = logRepository.findAll().stream()
                    .filter(logObject -> vacancyId.equals(logObject.getVacancyId())) // Compare Long
                    .filter(logObject -> logObject.getStatus() == LogStatus.REPORTED)
                    .collect(Collectors.toList());
            logger.info("Found {} logs for lecturer for vacancy ID: {} with status REPORTED", logs.size(), vacancyId);
            return logs;
        });
    }

    @Override
    public Log addMessageToLog(Long logId, String messageContent) {
        logger.info("Attempting to add message to log with ID: {}", logId);

        Log log = logRepository.findById(logId).orElseThrow(() -> {
            logger.warn("Log not found for adding message with ID: {}", logId);
            return new IllegalArgumentException("Log not found");
        });

        // Retrieve current studentId from SecurityContext for ownership check
        // This method is not @Async, so SecurityContextHolder access is generally safe here.
        // However, for consistency and testability, if this were to become async or complex,
        // passing studentId as a parameter (like in getAllLogsStudent) would be better.
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Long)) {
            // Handle cases where principal is not Long, e.g. if using UserDetails object
            // or if a different type of principal is expected.
            // This example assumes studentId is always Long.
            logger.error("Principal is not of type Long. Actual type: {}", principal.getClass().getName());
            throw new IllegalStateException("User principal is not of the expected type (Long).");
        }
        Long currentStudentId = (Long) principal;

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
    public List<String> getMessagesForLog(Long logId, Long userId, String userRole) {
        logger.info("Attempting to fetch messages for log with ID: {}, User ID: {}, Role: {}", logId, userId, userRole);
        Log log = logRepository.findById(logId).orElseThrow(() -> {
            logger.warn("Log not found for fetching messages with ID: {}", logId);
            return new IllegalArgumentException("Log not found");
        });

        // Authorization logic:
        // MAHASISWA can only see messages if they are the owner of the log.
        // DOSEN can see messages for any log (assuming this is the requirement).
        boolean authorized = false;
        if ("ROLE_MAHASISWA".equals(userRole)) {
            if (userId != null && userId.equals(log.getStudentId())) {
                authorized = true;
            }
        } else if ("ROLE_DOSEN".equals(userRole)) {
            authorized = true; // Dosen can view any log's messages
        }

        if (!authorized) {
            logger.warn("User {} (Role: {}) attempted to fetch messages for log {} (Owned by: {}). Access denied.",
                        userId, userRole, logId, log.getStudentId());
            throw new IllegalStateException("User not authorized to view messages for this log.");
        }

        logger.info("Successfully fetched {} messages for log ID: {}", log.getMessages().size(), logId);
        return new ArrayList<>(log.getMessages()); // Return a copy
    }
}
