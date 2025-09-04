package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestClient itemRequestClient;

    @InjectMocks
    private ItemRequestController itemRequestController;

    private ItemRequestDto itemRequestDto;
    private final Long userId = 1L;
    private final Long requestId = 1L;

    @BeforeEach
    void setUp() {
        itemRequestDto = ItemRequestDto.builder()
                .description("Test request description")
                .build();
    }

    @Test
    void create_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemRequestClient.create(eq(userId), any(ItemRequestDto.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestController.create(itemRequestDto, userId);

        assertEquals(expectedResponse, response);
        verify(itemRequestClient).create(userId, itemRequestDto);
    }

    @Test
    void getByUser_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemRequestClient.getByUser(eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestController.getByUser(userId);

        assertEquals(expectedResponse, response);
        verify(itemRequestClient).getByUser(userId);
    }

    @Test
    void getAll_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemRequestClient.getAll(eq(userId), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestController.getAll(userId, 0, 10);

        assertEquals(expectedResponse, response);
        verify(itemRequestClient).getAll(userId, 0, 10);
    }

    @Test
    void getById_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemRequestClient.getById(eq(userId), eq(requestId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemRequestController.getById(requestId, userId);

        assertEquals(expectedResponse, response);
        verify(itemRequestClient).getById(userId, requestId);
    }
}
