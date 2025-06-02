package id.ac.ui.cs.advprog.b13.hiringgo.log.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<Log, Long> {
    // JpaRepository provides save, findById, delete, findAll, etc.
    
    // Optimized query for student logs by studentId and vacancyId
    @Query("SELECT l FROM Log l WHERE l.studentId = :studentId AND l.vacancyId = :vacancyId ORDER BY l.logDate DESC, l.id DESC")
    List<Log> findByStudentIdAndVacancyIdOrderByLogDateDescIdDesc(@Param("studentId") Long studentId, @Param("vacancyId") Long vacancyId);
    
    // Optimized query for lecturer logs by vacancyId and status
    @Query("SELECT l FROM Log l WHERE l.vacancyId = :vacancyId AND l.status = :status ORDER BY l.logDate DESC, l.id DESC")
    List<Log> findByVacancyIdAndStatusOrderByLogDateDescIdDesc(@Param("vacancyId") Long vacancyId, @Param("status") LogStatus status);
    
    // Additional optimized queries for potential future use
    List<Log> findByStudentIdOrderByLogDateDescIdDesc(Long studentId);
    
    List<Log> findByVacancyIdOrderByLogDateDescIdDesc(Long vacancyId);
    
    List<Log> findByStatusOrderByLogDateDescIdDesc(LogStatus status);
}
