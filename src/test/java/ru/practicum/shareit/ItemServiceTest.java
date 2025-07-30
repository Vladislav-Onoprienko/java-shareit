package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepositoryImpl;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepositoryImpl;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {
    private ItemService itemService;
    private UserRepositoryImpl userRepository;
    private ItemRepositoryImpl itemRepository;

    @BeforeEach
    void setUp() {
        userRepository = new UserRepositoryImpl();
        itemRepository = new ItemRepositoryImpl();
        ItemMapper itemMapper = new ItemMapper();
        itemService = new ItemService(itemRepository, userRepository, itemMapper);
    }

    // Проверяем создание вещи
    @Test
    void createItem_ShouldSaveItem_WhenUserExists() {
        User owner = new User(1L, "owner@mail.com", "Owner");
        userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();

        ItemDto savedItem = itemService.createItem(itemDto, owner.getId());

        assertNotNull(savedItem.getId());
        assertEquals("Дрель", savedItem.getName());
    }

    // Проверяем частичное обновление вещи
    @Test
    void updateItem_ShouldUpdateName_WhenOnlyNameProvided() {
        User owner = new User(1L, "owner@mail.com", "Owner");
        userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
        ItemDto savedItem = itemService.createItem(itemDto, owner.getId());

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Дрель+")
                .build();

        ItemDto updatedItem = itemService.updateItem(savedItem.getId(), updateDto, owner.getId());

        assertEquals("Дрель+", updatedItem.getName());
        assertEquals("Простая дрель", updatedItem.getDescription());
        assertTrue(updatedItem.getAvailable());
    }

    // Проверяем обновления статуса available
    @Test
    void updateItem_ShouldUpdateAvailable_WhenOnlyStatusProvided() {
        User owner = new User(1L, "owner@mail.com", "Owner");
        userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
        ItemDto savedItem = itemService.createItem(itemDto, owner.getId());

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(savedItem.getId(), updateDto, owner.getId());

        assertFalse(updatedItem.getAvailable());
        assertEquals("Дрель", updatedItem.getName());
    }

    // Проверяем запрет обновления чужой вещи
    @Test
    void updateItem_ShouldThrowForbidden_WhenUserNotOwner() {
        User owner = new User(1L, "owner@mail.com", "Owner");
        userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Простая дрель")
                .available(true)
                .build();
        ItemDto savedItem = itemService.createItem(itemDto, owner.getId());

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Дрель+")
                .build();

        assertThrows(ForbiddenException.class, () ->
                itemService.updateItem(savedItem.getId(), updateDto, 999L));
    }

    // Проверяем получение несуществующей вещи
    @Test
    void getItemById_ShouldThrowNotFound_WhenItemNotExists() {
        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(999L));
    }
}
