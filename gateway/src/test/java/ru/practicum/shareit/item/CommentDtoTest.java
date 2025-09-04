package ru.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CommentDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void commentDto_ValidData_ShouldPassValidation() {
        CommentDto dto = CommentDto.builder()
                .text("Valid comment text")
                .authorName("Author Name")
                .created(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void commentDto_BlankText_ShouldFailValidation() {
        CommentDto dto = CommentDto.builder()
                .text("")
                .authorName("Author Name")
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Текст комментария не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void commentDto_NullText_ShouldFailValidation() {
        CommentDto dto = CommentDto.builder()
                .text(null)
                .authorName("Author Name")
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Текст комментария не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void commentDto_WithAuthor_ShouldBeValid() {
        UserDto author = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        CommentDto dto = CommentDto.builder()
                .text("Valid comment")
                .author(author)
                .authorName(author.getName())
                .build();

        Set<ConstraintViolation<CommentDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
        assertEquals("Test User", dto.getAuthorName());
        assertEquals(1L, dto.getAuthor().getId());
    }

    @Test
    void commentDto_Builder_ShouldWorkCorrectly() {
        CommentDto dto = CommentDto.builder()
                .id(1L)
                .text("Test comment")
                .authorName("Alex")
                .created(LocalDateTime.of(2023, 1, 1, 12, 0))
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Test comment", dto.getText());
        assertEquals("Alex", dto.getAuthorName());
        assertNotNull(dto.getCreated());
    }
}
