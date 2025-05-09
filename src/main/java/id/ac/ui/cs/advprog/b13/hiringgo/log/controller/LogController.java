package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogServiceImpl logService;
    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    public LogController(LogServiceImpl logService) {
        this.logService = logService;
    }

    // Endpoint for Mahasiswa to create a log
    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody Log log) {
        logger.info("Received request to create log: {}", log.getTitle());
        Log saved = logService.createLog(log);
        logger.info("Log created with ID: {}", saved.getId());
        return ResponseEntity.created(URI.create("/logs/" + saved.getId())).body(saved);
    }

    // Endpoint for Mahasiswa to update a log
    @PutMapping("/{id}")
    public ResponseEntity<Log> updateLog(@PathVariable Long id, @RequestBody Log log) {
        logger.info("Received request to update log with ID: {}", id);
        log.setId(id);
        Log updatedLog = logService.updateLog(log);
        logger.info("Log updated with ID: {}", updatedLog.getId());
        return ResponseEntity.ok(updatedLog);
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
    public ResponseEntity<Log> verifyLog(@PathVariable Long id,
                                         @RequestParam String action) {
        logger.info("Received request to verify log with ID: {} with action: {}", id, action);
        VerificationAction verificationAction = VerificationAction.valueOf(action.toUpperCase());
        Log verifiedLog = logService.verifyLog(id, verificationAction);
        logger.info("Log verified with ID: {}, new status: {}", verifiedLog.getId(), verifiedLog.getStatus());
        return ResponseEntity.ok(verifiedLog);
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

