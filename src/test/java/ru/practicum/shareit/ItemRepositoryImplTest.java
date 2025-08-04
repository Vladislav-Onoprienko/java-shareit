package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.ItemRepositoryImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemRepositoryImplTest {
    private ItemRepositoryImpl itemRepository;
    private User owner;
    private Item item1, item2;

    @BeforeEach
    void setUp() {
        itemRepository = new ItemRepositoryImpl();
        owner = new User(1L, "owner@mail.com", "Owner");
        item1 = new Item(1L, "Дрель", "Простая дрель", true, owner, null);
        item2 = new Item(2L, "Отвертка", "Крестовая", true, owner, null);
    }

    // Проверяем сохранение новой вещи
    @Test
    void save_ShouldAddNewItem() {
        Item savedItem = itemRepository.save(item1);
        assertEquals(item1, savedItem);
        assertEquals(1, itemRepository.findAllByOwnerId(owner.getId()).size());
    }

    // Проверяем обновление существующей вещи
    @Test
    void save_ShouldUpdateExistingItem() {
        itemRepository.save(item1);
        item1.setName("Дрель");
        Item updatedItem = itemRepository.save(item1);
        assertEquals("Дрель", updatedItem.getName());
    }

    // Проверяем поиск вещи по ID
    @Test
    void findById_ShouldReturnItem_WhenExists() {
        itemRepository.save(item1);
        assertTrue(itemRepository.findById(1L).isPresent());
        assertEquals(item1, itemRepository.findById(1L).get());
    }

    // Проверяем поиск несуществующей вещи
    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        assertTrue(itemRepository.findById(999L).isEmpty());
    }

    // Проверяем поиск всех вещей владельца
    @Test
    void findAllByOwnerId_ShouldReturnOwnerItems() {
        itemRepository.save(item1);
        itemRepository.save(item2);
        List<Item> items = itemRepository.findAllByOwnerId(owner.getId());
        assertEquals(2, items.size());
        assertTrue(items.contains(item1));
        assertTrue(items.contains(item2));
    }

    // Проверяем удаление вещи по ID
    @Test
    void deleteById_ShouldRemoveItem() {
        itemRepository.save(item1);
        assertTrue(itemRepository.deleteById(1L));
        assertTrue(itemRepository.findById(1L).isEmpty());
    }

    // Проверяем поиск вещей по тексту (название/описание)
    @Test
    void search_ShouldReturnMatchingItems() {
        itemRepository.save(item1);
        itemRepository.save(item2);

        List<Item> foundItems = itemRepository.search("дрель");
        assertEquals(1, foundItems.size());
        assertEquals(item1, foundItems.get(0));
    }

    // Проверяем поиск с пустым запросом (должен вернуть пустой список)
    @Test
    void search_ShouldReturnEmptyList_WhenTextIsBlank() {
        itemRepository.save(item1);
        assertTrue(itemRepository.search("").isEmpty());
    }

    // Проверяем, что поиск учитывает только доступные вещи (available = true)
    @Test
    void search_ShouldIgnoreUnavailableItems() {
        item1.setAvailable(false);
        itemRepository.save(item1);
        assertTrue(itemRepository.search("дрель").isEmpty());
    }
}
