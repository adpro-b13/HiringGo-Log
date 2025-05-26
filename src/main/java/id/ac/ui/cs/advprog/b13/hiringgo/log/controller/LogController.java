package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogService;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidationException;
import id.ac.ui.cs.advprog.b13.hiringgo.log.dto.MessageRequest; // Added import
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    public LogController(LogService logService) {
        this.logService = logService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MAHASISWA')") // Adjust roles as needed
    public ResponseEntity<Object> getLogById(@PathVariable Long id) {
        logger.info("Received request to get log with ID: {}", id);
        try {
            Log log = logService.getLogById(id);
            if (log != null) {
                Long currentUserId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                if (!log.getStudentId().equals(currentUserId)) {
                    logger.warn("User {} attempted to access unauthorized log ID {}", currentUserId, id);
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this log.");
                }
                logger.info("Returning log data for ID: {}", id);
                return ResponseEntity.ok(log);
            } else {
                logger.warn("Log not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving log with ID {}: {}", id, e.getMessage());
            // Return a generic error message for internal server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while retrieving the log.");
        }
    }

    // Endpoint for Mahasiswa to create a log
    @PostMapping("/{vacancyId}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Object> createLog(@PathVariable Long vacancyId, @Valid @RequestBody Log log, BindingResult bindingResult) {
        logger.info("Received request to create log for vacancyId {}: {}", vacancyId, log.getTitle());
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                                               .map(e -> e.getDefaultMessage())
                                               .toList();
            logger.warn("Validation errors when creating log: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); // Cast to Long
            log.setStudentId(studentId); // Set studentId from authenticated user
            log.setVacancyId(vacancyId); // Set vacancyId from path variable
            Log saved = logService.createLog(log).join(); // Await the async result
            logger.info("Log created with ID: {} for vacancyId: {}", saved.getId(), vacancyId);
            return ResponseEntity.created(URI.create("/logs/" + saved.getId())).body(saved);
        } catch (LogValidationException e) {
            logger.warn("Log validation exception when creating log: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint for Mahasiswa to update a log
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Object> updateLog(@PathVariable Long id, @Valid @RequestBody Log log, BindingResult bindingResult) {
        logger.info("Received request to update log with ID: {}", id);
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                                               .map(e -> e.getDefaultMessage())
                                               .toList();
            logger.warn("Validation errors when updating log ID {}: {}", id, errors);
            return ResponseEntity.badRequest().body(errors);
        }
        log.setId(id);
       
        try {
            Log updatedLog = logService.updateLog(log); // Removed .join()
            logger.info("Log updated with ID: {}", updatedLog.getId());
            return ResponseEntity.ok(updatedLog);
        } catch (LogValidationException e) {
            logger.warn("Log validation exception when updating log ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error updating log ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint for Mahasiswa to delete a log
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> deleteLog(@PathVariable Long id) {
        logger.info("Received request to delete log with ID: {}", id);
        logService.deleteLog(id);
        logger.info("Log deleted with ID: {}", id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Log berhasil dihapus");
        response.put("log_id", id.toString());
        return ResponseEntity.ok(response);
    }

    // Endpoint for Dosen to verify a log
    @PostMapping("/{id}/verify")
    @PreAuthorize("hasRole('DOSEN')")
    public ResponseEntity<Object> verifyLog(@PathVariable Long id,
                                         @RequestParam String action) {
        logger.info("Received request to verify log with ID: {} with action: {}", id, action);
        VerificationAction verificationAction;
        try {
            verificationAction = VerificationAction.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid action parameter for verification: {}", action);
            return ResponseEntity.badRequest().body("Invalid action value. Must be ACCEPT or REJECT.");
        }
        try {
            Log verifiedLog = logService.verifyLog(id, verificationAction); // Removed .join()
            logger.info("Log verified with ID: {}, new status: {}", verifiedLog.getId(), verifiedLog.getStatus());
            return ResponseEntity.ok(verifiedLog);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error verifying log ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Endpoint to list all logs for a student, optionally filtered by vacancyId
    @GetMapping("/student") // Changed mapping to avoid ambiguity
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<List<Log>> getAllLogsStudent(@RequestParam Long vacancyId) { // Changed String to Long
        Long studentId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // The service could take studentId to filter logs for the authenticated student.
        // For now, assuming service getAllLogsStudent(vacancyId) might implicitly use student context or needs update
        logger.info("Received request to get all logs for student {}, vacancyId: {}", studentId, vacancyId);
        List<Log> logs = logService.getAllLogsStudent(studentId, vacancyId).join();
        logger.info("Returning {} logs for student {}, vacancyId {}", logs.size(), studentId, vacancyId);
        return ResponseEntity.ok(logs);
    }

    // Endpoint to list all logs for a lecturer, filtered by vacancyId and status REPORTED
    @GetMapping("/lecturer")
    @PreAuthorize("hasRole('DOSEN')")
    public ResponseEntity<List<Log>> getAllLogsLecturer(@RequestParam Long vacancyId) { // Changed String to Long
        logger.info("Received request to get all logs for lecturer, vacancyId: {}", vacancyId);
        List<Log> logs = logService.getAllLogsLecturer(vacancyId).join();
        logger.info("Returning {} logs for lecturer for vacancyId {}", logs.size(), vacancyId);
        return ResponseEntity.ok(logs);
    }

    // Endpoint for Mahasiswa to add a message to a log
    @PostMapping("/{id}/messages")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Object> addMessageToLog(@PathVariable Long id,
                                             @Valid @RequestBody MessageRequest messageRequest,
                                             BindingResult bindingResult) {
        logger.info("Received request to add message to log with ID: {}", id);
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                                               .map(e -> e.getDefaultMessage())
                                               .toList();
            logger.warn("Validation errors when adding message to log ID {}: {}", id, errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            Log updatedLog = logService.addMessageToLog(id, messageRequest.getMessage()); // Removed .join()
            logger.info("Message successfully added to log ID: {}. Returning updated log.", updatedLog.getId());
            return ResponseEntity.ok(updatedLog);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error adding message to log ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint to get all messages for a specific log
    @GetMapping("/{id}/messages")
    @PreAuthorize("hasAnyRole('MAHASISWA', 'DOSEN')")
    public ResponseEntity<Object> getMessagesForLog(@PathVariable Long id) {
        logger.info("Received request to get messages for log with ID: {}", id);
        try {
            // Pass current user's ID and role to service for authorization checks
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator().next().getAuthority();
            
            // Assuming principal is the user ID (Long for MAHASISWA, could be different for DOSEN)
            // For simplicity, we'll pass the principal object and let service handle type checking if necessary,
            // or standardize on Long if DOSEN also uses Long IDs.
            // If DOSEN principal is not Long, this needs adjustment or a more generic way to pass user identifier.
            Long userId = (principal instanceof Long) ? (Long) principal : null; // Example, adjust as per your UserDetails
            if (userId == null && "ROLE_MAHASISWA".equals(role)) {
                 // This case should ideally not happen if principal is always Long for MAHASISWA
                 logger.error("MAHASISWA role without Long principal for log message access.");
                 return ResponseEntity.status(500).body("Internal server error: User principal misconfiguration.");
            }


            List<String> messages = logService.getMessagesForLog(id, userId, role);
            logger.info("Successfully retrieved {} messages for log ID: {}", messages.size(), id);
            return ResponseEntity.ok(messages);
        } catch (IllegalArgumentException e) { // Log not found
            logger.warn("Log not found when trying to get messages for log ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (IllegalStateException e) { // Unauthorized
            logger.warn("Unauthorized attempt to get messages for log ID {}: {}", id, e.getMessage());
            // Consider 403 Forbidden, but for consistency with other IllegalStateExceptions here, using 400.
            // A custom exception handler could map this specific IllegalStateException to 403.
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

