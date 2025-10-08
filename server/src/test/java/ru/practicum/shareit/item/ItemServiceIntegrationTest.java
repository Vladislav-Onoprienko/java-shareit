package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createAndGetItem_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto created = itemService.createItem(itemDto, savedOwner.getId());
        assertNotNull(created.getId());
        assertEquals("Drill", created.getName());

        ItemDto found = itemService.getItemById(created.getId(), savedOwner.getId());
        assertEquals(created.getId(), found.getId());
    }

    @Test
    void updateItem_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        ItemDto created = itemService.createItem(itemDto, savedOwner.getId());

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Hammer")
                .build();

        ItemDto updated = itemService.updateItem(created.getId(), updateDto, savedOwner.getId());
        assertEquals("Updated Hammer", updated.getName());
    }

    @Test
    void updateItem_ShouldThrowWhenNotOwner() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        User otherUser = new User();
        otherUser.setName("Alice");
        otherUser.setEmail("alice@email.com");
        User savedOtherUser = userRepository.save(otherUser);

        ItemDto itemDto = ItemDto.builder()
                .name("Saw")
                .description("Sharp saw")
                .available(true)
                .build();

        ItemDto created = itemService.createItem(itemDto, savedOwner.getId());

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Saw")
                .build();

        assertThrows(ForbiddenException.class, () ->
                itemService.updateItem(created.getId(), updateDto, savedOtherUser.getId()));
    }

    @Test
    void getAllItemsByOwner_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        ItemDto item1 = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto item2 = ItemDto.builder()
                .name("Hammer")
                .description("Heavy hammer")
                .available(true)
                .build();

        itemService.createItem(item1, savedOwner.getId());
        itemService.createItem(item2, savedOwner.getId());

        List<ItemDto> items = itemService.getAllItemsByOwner(savedOwner.getId());
        assertEquals(2, items.size());
    }

    @Test
    void searchItems_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        ItemDto itemDto = ItemDto.builder()
                .name("Electric Drill")
                .description("Powerful electric drill")
                .available(true)
                .build();

        itemService.createItem(itemDto, savedOwner.getId());

        List<ItemDto> results = itemService.searchItems("drill");
        assertFalse(results.isEmpty());
        assertEquals("Electric Drill", results.get(0).getName());
    }
}
