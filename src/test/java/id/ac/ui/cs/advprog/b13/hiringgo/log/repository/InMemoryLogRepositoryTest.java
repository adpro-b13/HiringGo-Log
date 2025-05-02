package id.ac.ui.cs.advprog.b13.hiringgo.log.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryLogRepositoryTest {
    private InMemoryLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryLogRepository();
    }

    @Test
    void whenSaveNewLog_thenIdAssignedAndCanBeFound() {
        Log log = new Log("Title", "Desc", "Cat", "VAC-1",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now());
        assertNull(log.getId());

        Log saved = repository.save(log);
        assertNotNull(saved.getId(), "ID should be generated");
        assertEquals(saved, repository.findById(saved.getId()));
    }

    @Test
    void whenSaveMultiple_thenIdsIncrement() {
        Log first = repository.save(new Log("A","B","C","V",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now()));
        Log second = repository.save(new Log("X","Y","Z","V",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now()));

        assertTrue(second.getId() > first.getId(), "Second ID should be greater than first");
    }

    @Test
    void whenDeleteLog_thenCannotBeFound() {
        Log log = repository.save(new Log("T","D","C","V",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now()));
        Long id = log.getId();
        assertNotNull(repository.findById(id));

        repository.delete(log);
        assertNull(repository.findById(id), "Deleted log should not be found");
    }

    @Test
    void whenFindAll_thenReturnAllSaved() {
        repository.save(new Log("1","D1","C","V",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now()));
        repository.save(new Log("2","D2","C","V",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), LocalDate.now()));

        List<Log> all = repository.findAll();
        assertEquals(2, all.size());
    }
}
