package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemRequestIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void createAndGetRequest_ShouldWork() {
        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@email.com");
        User savedUser = userRepository.save(user);

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        ItemRequestDto created = itemRequestService.create(savedUser.getId(), requestDto);
        assertNotNull(created.getId());
        assertEquals("Need a drill", created.getDescription());

        List<ItemRequestDto> requests = itemRequestService.getByUser(savedUser.getId());
        assertEquals(1, requests.size());
        assertEquals(created.getId(), requests.get(0).getId());
    }

    @Test
    void getAllRequests_ShouldWork() {
        User user1 = new User();
        user1.setName("Alex");
        user1.setEmail("alex@email.com");
        User savedUser1 = userRepository.save(user1);

        User user2 = new User();
        user2.setName("Alice");
        user2.setEmail("alice@email.com");
        User savedUser2 = userRepository.save(user2);

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a hammer")
                .build();

        itemRequestService.create(savedUser2.getId(), requestDto);

        List<ItemRequestDto> requests = itemRequestService.getAll(savedUser1.getId(), 0, 10);
        assertFalse(requests.isEmpty());
    }

    @Test
    void getRequestById_ShouldWork() {
        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@email.com");
        User savedUser = userRepository.save(user);

        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a saw")
                .build();

        ItemRequestDto created = itemRequestService.create(savedUser.getId(), requestDto);

        ItemRequestDto found = itemRequestService.getById(savedUser.getId(), created.getId());
        assertEquals(created.getId(), found.getId());
        assertEquals("Need a saw", found.getDescription());
    }

    @Test
    void getRequestById_ShouldThrowWhenNotFound() {
        User user = new User();
        user.setName("Alex");
        user.setEmail("alex@email.com");
        User savedUser = userRepository.save(user);

        assertThrows(NotFoundException.class, () ->
                itemRequestService.getById(savedUser.getId(), 999L));
    }
}