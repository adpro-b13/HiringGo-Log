package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogService;
import id.ac.ui.cs.advprog.b13.hiringgo.log.validator.LogValidationException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    public LogController(LogService logService) {
        this.logService = logService;
    }

    // Endpoint for Mahasiswa to create a log
    @PostMapping
    public ResponseEntity<?> createLog(@Valid @RequestBody Log log, BindingResult bindingResult) {
        logger.info("Received request to create log: {}", log.getTitle());
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                                               .map(e -> e.getDefaultMessage())
                                               .collect(Collectors.toList());
            logger.warn("Validation errors when creating log: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }
        try {
            Log saved = logService.createLog(log);
            logger.info("Log created with ID: {}", saved.getId());
            return ResponseEntity.created(URI.create("/logs/" + saved.getId())).body(saved);
        } catch (LogValidationException e) {
            logger.warn("Log validation exception when creating log: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint for Mahasiswa to update a log
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateLog(@PathVariable Long id, @Valid @RequestBody Log log, BindingResult bindingResult) {
        logger.info("Received request to update log with ID: {}", id);
        if (bindingResult.hasErrors()) {
            List<String> errors = bindingResult.getAllErrors().stream()
                                               .map(e -> e.getDefaultMessage())
                                               .collect(Collectors.toList());
            logger.warn("Validation errors when updating log ID {}: {}", id, errors);
            return ResponseEntity.badRequest().body(errors);
        }
        log.setId(id);
        try {
            Log updatedLog = logService.updateLog(log);
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
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        logger.info("Received request to delete log with ID: {}", id);
        logService.deleteLog(id);
        logger.info("Log deleted with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint for Dosen to verify a log
    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verifyLog(@PathVariable Long id,
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
            Log verifiedLog = logService.verifyLog(id, verificationAction);
            logger.info("Log verified with ID: {}, new status: {}", verifiedLog.getId(), verifiedLog.getStatus());
            return ResponseEntity.ok(verifiedLog);
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Error verifying log ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint to list all logs
    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        logger.info("Received request to get all logs");
        List<Log> logs = logService.getAllLogs();
        logger.info("Returning {} logs", logs.size());
        return ResponseEntity.ok(logs);
    }
}

