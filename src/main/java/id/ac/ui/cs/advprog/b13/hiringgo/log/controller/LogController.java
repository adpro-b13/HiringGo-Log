package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.LogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogController {

    private final LogService logService;

    public LogController(LogService logService) {
        this.logService = logService;
    }

    // Endpoint for Mahasiswa to create a log
    @PostMapping
    public ResponseEntity<Log> createLog(@RequestBody Log log) {
        Log saved = logService.createLog(log);
        return ResponseEntity.created(URI.create("/logs/" + saved.getId())).body(saved);
    }

    // Endpoint for Mahasiswa to update a log
    @PutMapping("/{id}")
    public ResponseEntity<Log> updateLog(@PathVariable Long id, @RequestBody Log log) {
        log.setId(id);
        return ResponseEntity.ok(logService.updateLog(log));
    }

    // Endpoint for Mahasiswa to delete a log
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        logService.deleteLog(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint for Dosen to verify a log
    @PostMapping("/{id}/verify")
    public ResponseEntity<Log> verifyLog(@PathVariable Long id,
                                         @RequestParam String action) {
        // Expect action to be "ACCEPT" or "REJECT"
        VerificationAction verificationAction = VerificationAction.valueOf(action.toUpperCase());
        return ResponseEntity.ok(logService.verifyLog(id, verificationAction));
    }

    // Endpoint to list all logs
    @GetMapping
    public ResponseEntity<List<Log>> getAllLogs() {
        return ResponseEntity.ok(logService.getAllLogs());
    }
}

