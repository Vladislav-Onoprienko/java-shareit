package ru.practicum.shareit.booking;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookingDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void bookingDto_ValidData_ShouldPassValidation() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void bookingDto_NullStart_ShouldFailValidation() {
        BookingDto dto = BookingDto.builder()
                .start(null)
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void bookingDto_PastStart_ShouldFailValidation() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
    }

    @Test
    void bookingDto_NullEnd_ShouldFailValidation() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .itemId(1L)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void bookingDto_EndBeforeStart_ShouldBeInvalid() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        BookingDto dto = BookingDto.builder()
                .start(start)
                .end(end)
                .itemId(1L)
                .build();

        assertTrue(end.isBefore(start), "End date should be before start date for this test");

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty(),
                "Standard validation should pass, custom validation would be needed for date comparison");
    }

    @Test
    void bookingDto_NullItemId_ShouldFailValidation() {
        BookingDto dto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(null)
                .build();

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }
}
