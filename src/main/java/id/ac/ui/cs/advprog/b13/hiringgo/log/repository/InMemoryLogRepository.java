package id.ac.ui.cs.advprog.b13.hiringgo.log.repository;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLogRepository implements LogRepository {
    private final ConcurrentHashMap<Long, Log> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Log save(Log log) {
        if (log.getId() == null) {
            log.setId(idGenerator.getAndIncrement());
        }
        storage.put(log.getId(), log);
        return log;
    }

    @Override
    public Log findById(Long id) {
        return storage.get(id);
    }

    @Override
    public void delete(Log log) {
        if (log.getId() != null) {
            storage.remove(log.getId());
        }
    }

    @Override
    public List<Log> findAll() {
        return new ArrayList<>(storage.values());
    }
}
