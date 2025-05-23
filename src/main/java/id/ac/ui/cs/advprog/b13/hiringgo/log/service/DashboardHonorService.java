package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonor;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonorSummary;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DashboardHonorService {

    List<DashboardHonor> getDashboardHonor(int year, int month);

    DashboardHonorSummary getDashboardHonorSummary(int year, int month);

    CompletableFuture<List<DashboardHonor>> getDashboardHonorAsync(int year, int month);
    CompletableFuture<DashboardHonorSummary> getDashboardHonorSummaryAsync(int year, int month);
}