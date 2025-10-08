package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static org.junit.jupiter.api.Assertions.*;

class UpdateItemDtoTest {

    @Test
    void updateItemDto_Builder_ShouldWorkCorrectly() {
        UpdateItemDto dto = UpdateItemDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        assertEquals("Updated Name", dto.getName());
        assertEquals("Updated Description", dto.getDescription());
        assertFalse(dto.getAvailable());
    }

    @Test
    void updateItemDto_WithNullValues_ShouldBeValid() {
        UpdateItemDto dto = UpdateItemDto.builder()
                .name(null)
                .description(null)
                .available(null)
                .build();

        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getAvailable());
    }

    @Test
    void updateItemDto_WithPartialData_ShouldBeValid() {
        UpdateItemDto dto = UpdateItemDto.builder()
                .name("Only Name Updated")
                .build();

        assertEquals("Only Name Updated", dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getAvailable());
    }

    @Test
    void updateItemDto_EqualsAndHashCode_ShouldWorkCorrectly() {
        UpdateItemDto dto1 = UpdateItemDto.builder()
                .name("Test")
                .description("Desc")
                .available(true)
                .build();

        UpdateItemDto dto2 = UpdateItemDto.builder()
                .name("Test")
                .description("Desc")
                .available(true)
                .build();

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void updateItemDto_ToString_ShouldContainFields() {
        UpdateItemDto dto = UpdateItemDto.builder()
                .name("Test")
                .description("Desc")
                .available(true)
                .build();

        String toString = dto.toString();
        assertTrue(toString.contains("Test"));
        assertTrue(toString.contains("Desc"));
        assertTrue(toString.contains("true"));
    }
}
