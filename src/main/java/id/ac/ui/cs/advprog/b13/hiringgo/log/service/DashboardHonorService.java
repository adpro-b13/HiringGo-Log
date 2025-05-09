package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonor;
import id.ac.ui.cs.advprog.b13.hiringgo.log.model.DashboardHonorSummary;
import java.util.List;

public interface DashboardHonorService {

    List<DashboardHonor> getDashboardHonor(int year, int month);

    DashboardHonorSummary getDashboardHonorSummary(int year, int month);
}