package id.ac.ui.cs.advprog.b13.hiringgo.log.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    // JpaRepository provides save, findById, delete, findAll, etc.
    // Custom query methods can be added here if needed.
}
