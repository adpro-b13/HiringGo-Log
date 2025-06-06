package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.state.VerificationAction;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface LogService {
    Log getLogById(Long id);
    CompletableFuture<Log> createLog(Log log);
    Log updateLog(Log log);
    void deleteLog(Long id);
    Log verifyLog(Long id, VerificationAction action);
    CompletableFuture<List<Log>> getAllLogsStudent(Long studentId, Long vacancyId); // Changed String to Long
    CompletableFuture<List<Log>> getAllLogsLecturer(Long vacancyId); // Changed String to Long
    Log addMessageToLog(Long logId, String message); // Added method
    List<String> getMessagesForLog(Long logId, Long userId, String userRole); // New method
}
