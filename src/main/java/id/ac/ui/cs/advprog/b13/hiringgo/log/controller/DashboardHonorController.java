package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonor;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonorSummary;
import id.ac.ui.cs.advprog.b13.hiringgo.log.service.DashboardHonorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/dashboard/honor")
public class DashboardHonorController {

    private final DashboardHonorService dashboardHonorService;

    public DashboardHonorController(DashboardHonorService dashboardHonorService) {
        this.dashboardHonorService = dashboardHonorService;
    }

    @GetMapping("/details")
    public ResponseEntity<List<DashboardHonor>> getDashboardHonorDetails(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        int yearValue = year != null ? year : LocalDate.now().getYear();
        int monthValue = month != null ? month : LocalDate.now().getMonthValue();

        if (monthValue < 1 || monthValue > 12) {
            return ResponseEntity.badRequest().build();
        }

        List<DashboardHonor> dashboardHonor = dashboardHonorService.getDashboardHonor(yearValue, monthValue);
        return ResponseEntity.ok(dashboardHonor);
    }

    @GetMapping
    public ResponseEntity<DashboardHonorSummary> getDashboardHonorSummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        int yearValue = year != null ? year : LocalDate.now().getYear();
        int monthValue = month != null ? month : LocalDate.now().getMonthValue();

        if (monthValue < 1 || monthValue > 12) {
            return ResponseEntity.badRequest().build();
        }

        DashboardHonorSummary summary = dashboardHonorService.getDashboardHonorSummary(yearValue, monthValue);
        return ResponseEntity.ok(summary);
    }
}