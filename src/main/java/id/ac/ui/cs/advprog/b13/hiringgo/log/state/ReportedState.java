package id.ac.ui.cs.advprog.b13.hiringgo.log.state;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;

public class ReportedState implements LogState {

    @Override
    public LogStatus verify(VerificationAction action) {
        switch (action) {
            case ACCEPT:
                return LogStatus.ACCEPTED;
            case REJECT:
                return LogStatus.REJECTED;
            default:
                throw new IllegalArgumentException("Invalid action for verification.");
        }
    }

    @Override
    public LogStatus stateName() {
        return LogStatus.REPORTED;
    }
}

