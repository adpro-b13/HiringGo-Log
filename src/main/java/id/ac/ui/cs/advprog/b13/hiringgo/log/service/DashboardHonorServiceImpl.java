package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonor;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonorSummary;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.Log;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;
import id.ac.ui.cs.advprog.b13.hiringgo.log.repository.LogRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardHonorServiceImpl implements DashboardHonorService {
    private static final BigDecimal HOURLY_RATE = new BigDecimal("27500");

    private final LogRepository logRepository;
    private final UserService userService;


    public DashboardHonorServiceImpl(LogRepository logRepository, UserService userService) {
        this.logRepository = logRepository;
        this.userService = userService;
    }

    @Override
    public List<DashboardHonor> getDashboardHonor(int year, int month) {
        String studentId = userService.getCurrentStudentId();

        List<Log> allLogs = logRepository.findAll().stream()
                .filter(log -> studentId.equals(log.getStudentId()))
                .filter(log -> log.getStatus() == LogStatus.ACCEPTED)
                .filter(log -> log.getLogDate().getYear() == year && log.getLogDate().getMonthValue() == month)
                .collect(Collectors.toList());

        Map<String, List<Log>> logsByVacancy = new HashMap<>();
        for (Log log : allLogs) {
            logsByVacancy.computeIfAbsent(log.getVacancyId(), k -> new ArrayList<>()).add(log);
        }

        List<DashboardHonor> dashboardHonors = new ArrayList<>();
        for (Map.Entry<String, List<Log>> entry : logsByVacancy.entrySet()) {
            String vacancyId = entry.getKey();
            List<Log> logs = entry.getValue();

            String vacancyTitle = "Vacancy " + vacancyId; // Placeholder

            // Calculate total hours
            long totalMinutes = 0;
            for (Log log : logs) {
                Duration duration = Duration.between(log.getStartTime(), log.getEndTime());
                totalMinutes += duration.toMinutes();
            }

            long totalHours = totalMinutes / 60;
            long remainingMinutes = totalMinutes % 60;

            BigDecimal hoursPrecise = new BigDecimal(totalHours + (remainingMinutes / 60.0));
            BigDecimal totalHonor = HOURLY_RATE.multiply(hoursPrecise);

            dashboardHonors.add(new DashboardHonor(
                    vacancyId,
                    vacancyTitle,
                    year,
                    Month.of(month),
                    totalHonor,
                    totalHours
            ));
        }

        return dashboardHonors;
    }

    @Override
    public DashboardHonorSummary getDashboardHonorSummary(int year, int month) {
        List<DashboardHonor> details = getDashboardHonor(year, month);

        BigDecimal totalHonor = details.stream()
                .map(DashboardHonor::getTotalHonor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalHours = details.stream()
                .mapToLong(DashboardHonor::getTotalHours)
                .sum();

        return new DashboardHonorSummary(
                year,
                Month.of(month),
                totalHonor,
                totalHours,
                details
        );
    }
}