package id.ac.ui.cs.advprog.b13.hiringgo.log.state;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;

public interface LogState {
    LogStatus verify(VerificationAction action);
    LogStatus stateName();
}