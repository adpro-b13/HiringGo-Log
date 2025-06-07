package id.ac.ui.cs.advprog.b13.hiringgo.log.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final Long DUMMY_STUDENT_ID = 123L; // Changed to Long

    @Override
    public Long getCurrentStudentId() { // Changed return type to Long
        // This is a dummy implementation.
        // In a real scenario, this would involve security context or similar to get the authenticated user.
        logger.info("UserServiceImpl: Returning dummy student ID: {}", DUMMY_STUDENT_ID);
        return DUMMY_STUDENT_ID;
    }
}
