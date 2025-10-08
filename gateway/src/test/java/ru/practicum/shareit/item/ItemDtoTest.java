package ru.practicum.shareit.item;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void itemDto_ValidData_ShouldPassValidation() {
        ItemDto dto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void itemDto_BlankName_ShouldFailValidation() {
        ItemDto dto = ItemDto.builder()
                .name("")
                .description("Test Description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void itemDto_NullName_ShouldFailValidation() {
        ItemDto dto = ItemDto.builder()
                .name(null)
                .description("Test Description")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void itemDto_BlankDescription_ShouldFailValidation() {
        ItemDto dto = ItemDto.builder()
                .name("Test Item")
                .description("")
                .available(true)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Описание не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void itemDto_NullAvailable_ShouldFailValidation() {
        ItemDto dto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(null)
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Статус аренды обязателен", violations.iterator().next().getMessage());
    }

    @Test
    void itemDto_WithBookingsAndComments_ShouldWorkCorrectly() {
        BookingShortDto lastBooking = BookingShortDto.builder()
                .id(1L)
                .bookerId(2L)
                .build();

        BookingShortDto nextBooking = BookingShortDto.builder()
                .id(3L)
                .bookerId(4L)
                .build();

        CommentDto comment = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("User")
                .build();

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .ownerId(5L)
                .requestId(6L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(comment))
                .build();

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());

        assertEquals(1L, dto.getId());
        assertEquals(5L, dto.getOwnerId());
        assertEquals(6L, dto.getRequestId());
        assertNotNull(dto.getLastBooking());
        assertNotNull(dto.getNextBooking());
        assertEquals(1, dto.getComments().size());
        assertEquals("Great item!", dto.getComments().get(0).getText());
    }
}