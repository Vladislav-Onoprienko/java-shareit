package ru.practicum.shareit.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void itemRequestDto_ValidData_ShouldPassValidation() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("Need a drill for home renovation")
                .created(LocalDateTime.now())
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void itemRequestDto_BlankDescription_ShouldFailValidation() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description("")
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Описание запроса не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void itemRequestDto_NullDescription_ShouldFailValidation() {
        ItemRequestDto dto = ItemRequestDto.builder()
                .description(null)
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Описание запроса не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void itemRequestDto_WithItems_ShouldWorkCorrectly() {
        ItemRequestDto.ItemResponseDto item = ItemRequestDto.ItemResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(2L)
                .requestId(3L)
                .build();

        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Need tools")
                .created(LocalDateTime.now())
                .items(List.of(item))
                .build();

        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());

        assertEquals(1L, dto.getId());
        assertEquals("Need tools", dto.getDescription());
        assertNotNull(dto.getCreated());
        assertEquals(1, dto.getItems().size());
        assertEquals("Drill", dto.getItems().get(0).getName());
    }

    @Test
    void itemResponseDto_Builder_ShouldWorkCorrectly() {
        ItemRequestDto.ItemResponseDto item = ItemRequestDto.ItemResponseDto.builder()
                .id(1L)
                .name("Hammer")
                .description("Heavy hammer")
                .available(false)
                .ownerId(5L)
                .requestId(10L)
                .build();

        assertEquals(1L, item.getId());
        assertEquals("Hammer", item.getName());
        assertEquals("Heavy hammer", item.getDescription());
        assertFalse(item.getAvailable());
        assertEquals(5L, item.getOwnerId());
        assertEquals(10L, item.getRequestId());
    }

    @Test
    void itemRequestDto_DefaultItemsList_ShouldBeEmpty() {
        ItemRequestDto dto = new ItemRequestDto();
        assertNotNull(dto.getItems());
        assertTrue(dto.getItems().isEmpty());
    }
}