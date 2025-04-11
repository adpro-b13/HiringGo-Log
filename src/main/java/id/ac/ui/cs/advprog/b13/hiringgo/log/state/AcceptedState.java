package id.ac.ui.cs.advprog.b13.hiringgo.log.state;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;

public class AcceptedState implements LogState {

    @Override
    public LogStatus verify(VerificationAction action) {
        throw new IllegalStateException("Log is already accepted; no further verification allowed.");
    }

    @Override
    public LogStatus stateName() {
        return LogStatus.ACCEPTED;
    }
}

