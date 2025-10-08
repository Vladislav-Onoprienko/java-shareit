package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestService itemRequestService;

    @Test
    void create_ShouldCreateRequest() {
        Long userId = 1L;
        User requester = new User(userId, "Alex", "alex@email.com");
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need a drill");
        request.setRequestor(requester);
        request.setCreated(LocalDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(requester));
        when(itemRequestMapper.toEntity(requestDto)).thenReturn(request);
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .build());

        ItemRequestDto result = itemRequestService.create(userId, requestDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(itemRequestRepository).save(any(ItemRequest.class));
    }

    @Test
    void create_ShouldThrowWhenUserNotFound() {
        Long userId = 1L;
        ItemRequestDto requestDto = ItemRequestDto.builder().description("test").build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemRequestService.create(userId, requestDto));
    }

    @Test
    void getByUser_ShouldReturnRequests() {
        Long userId = 1L;
        User user = new User(userId, "Alex", "alex@email.com");
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setRequestor(user);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder().id(1L).build());
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getByUser(userId);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAll_ShouldReturnRequests() {
        Long userId = 1L;
        ItemRequest request = new ItemRequest();
        request.setId(1L);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(List.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder().id(1L).build());
        when(itemRepository.findByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestDto> result = itemRequestService.getAll(userId, 0, 10);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getById_ShouldReturnRequest() {
        Long userId = 1L;
        Long requestId = 1L;
        ItemRequest request = new ItemRequest();
        request.setId(requestId);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder().id(requestId).build());
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of());

        ItemRequestDto result = itemRequestService.getById(userId, requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
    }

    @Test
    void getByUser_ShouldEnrichWithItems() {
        Long userId = 1L;
        User user = new User(userId, "Alex", "alex@email.com");
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setRequestor(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test description");
        item.setAvailable(true);
        item.setOwner(user);
        item.setRequest(request);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId)).thenReturn(List.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder().id(1L).build());
        when(itemRepository.findByRequestIdIn(List.of(1L))).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getByUser(userId);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(itemRepository).findByRequestIdIn(List.of(1L)); // Проверяем, что enrichWithItems вызван
    }

    @Test
    void getById_ShouldEnrichWithItems() {
        Long userId = 1L;
        Long requestId = 1L;
        User user = new User(userId, "Alex", "alex@email.com");
        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setRequestor(user);

        Item item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test description");
        item.setAvailable(true);
        item.setOwner(user);
        item.setRequest(request);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder().id(requestId).build());
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getById(userId, requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        verify(itemRepository).findByRequestId(requestId);
    }

    @Test
    void getById_ShouldHandleItemWithNullValues() {
        Long userId = 1L;
        Long requestId = 1L;
        User user = new User(userId, "Alex", "alex@email.com");
        ItemRequest request = new ItemRequest();
        request.setId(requestId);
        request.setRequestor(user);

        Item item = new Item();
        item.setId(1L);
        item.setName(null);
        item.setDescription("Test description");
        item.setAvailable(true);
        item.setOwner(null);
        item.setRequest(request);

        when(userRepository.existsById(userId)).thenReturn(true);
        when(itemRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRequestMapper.toDto(request)).thenReturn(ItemRequestDto.builder().id(requestId).build());
        when(itemRepository.findByRequestId(requestId)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getById(userId, requestId);

        assertNotNull(result);
        assertEquals(requestId, result.getId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());

        ItemRequestDto.ItemResponseDto itemResponse = result.getItems().get(0);
        assertEquals("Default Name", itemResponse.getName());
        assertNull(itemResponse.getOwnerId());
    }
}
