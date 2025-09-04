package ru.practicum.shareit.user;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void userDto_ValidData_ShouldPassValidation() {
        UserDto dto = UserDto.builder()
                .name("Alex")
                .email("alex.doe@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void userDto_BlankName_ShouldFailValidation() {
        UserDto dto = UserDto.builder()
                .name("")
                .email("alex.doe@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Имя не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void userDto_NullName_ShouldFailValidation() {
        UserDto dto = UserDto.builder()
                .name(null)
                .email("alex.doe@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Имя не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void userDto_BlankEmail_ShouldFailValidation() {
        UserDto dto = UserDto.builder()
                .name("Alex")
                .email("")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void userDto_InvalidEmail_ShouldFailValidation() {
        UserDto dto = UserDto.builder()
                .name("Alex")
                .email("invalid-email")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Некорректный формат email", violations.iterator().next().getMessage());
    }

    @Test
    void userDto_NullEmail_ShouldFailValidation() {
        UserDto dto = UserDto.builder()
                .name("Alex")
                .email(null) // null email
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Email не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void userDto_WithId_ShouldWorkCorrectly() {
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("Alex")
                .email("alex.doe@example.com")
                .build();

        Set<ConstraintViolation<UserDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertEquals(1L, dto.getId());
    }

    @Test
    void userDto_AllArgsConstructor_ShouldWork() {
        UserDto dto = new UserDto(1L, "Alex", "alex@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("Alex", dto.getName());
        assertEquals("alex@example.com", dto.getEmail());
    }

    @Test
    void userDto_NoArgsConstructor_ShouldWork() {
        UserDto dto = new UserDto();
        dto.setId(1L);
        dto.setName("Test");
        dto.setEmail("test@example.com");

        assertEquals(1L, dto.getId());
        assertEquals("Test", dto.getName());
        assertEquals("test@example.com", dto.getEmail());
    }
}
