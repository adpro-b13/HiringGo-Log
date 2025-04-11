package id.ac.ui.cs.advprog.b13.hiringgo.log.state;

import id.ac.ui.cs.advprog.b13.hiringgo.log.model.LogStatus;

public class LogStateFactory {
    public static LogState getState(LogStatus status) {
        switch (status) {
            case REPORTED:
                return new ReportedState();
            case ACCEPTED:
                return new AcceptedState();
            case REJECTED:
                return new RejectedState();
            default:
                throw new IllegalArgumentException("Unknown log status: " + status);
        }
    }
}

