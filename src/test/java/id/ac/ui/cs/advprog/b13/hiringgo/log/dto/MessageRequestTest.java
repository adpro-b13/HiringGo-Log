package id.ac.ui.cs.advprog.b13.hiringgo.log.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MessageRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testGetAndSetMessage() {
        MessageRequest request = new MessageRequest();
        String testMessage = "Hello, World!";
        request.setMessage(testMessage);
        assertEquals(testMessage, request.getMessage(), "The message retrieved should be the same as the message set.");
    }

    @Test
    void testMessageNotBlank_whenValid() {
        MessageRequest request = new MessageRequest();
        request.setMessage("This is a valid message.");
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "There should be no violations for a valid message.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void testMessageNotBlank_whenInvalid(String invalidMessage) {
        MessageRequest request = new MessageRequest();
        request.setMessage(invalidMessage);
        Set<ConstraintViolation<MessageRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "There should be a violation for an invalid message: '" + invalidMessage + "'");
        ConstraintViolation<MessageRequest> violation = violations.iterator().next();
        assertEquals("Message cannot be blank", violation.getMessage());
        assertEquals("message", violation.getPropertyPath().toString());
    }
}
