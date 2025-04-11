package id.ac.ui.cs.advprog.b13.hiringgo.log.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import java.util.List;

public interface LogRepository {
    Log save(Log log);
    Log findById(Long id);
    void delete(Log log);
    List<Log> findAll();
}
