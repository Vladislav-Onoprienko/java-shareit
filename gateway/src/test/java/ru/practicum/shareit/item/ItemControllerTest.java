package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemClient itemClient;

    @InjectMocks
    private ItemController itemController;

    private ItemDto itemDto;
    private UpdateItemDto updateItemDto;
    private CommentDto commentDto;
    private final Long userId = 1L;
    private final Long itemId = 1L;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();

        updateItemDto = UpdateItemDto.builder()
                .name("Updated Item")
                .description("Updated Description")
                .available(false)
                .build();

        commentDto = CommentDto.builder()
                .text("Test comment")
                .build();
    }

    @Test
    void createItem_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemClient.createItem(any(ItemDto.class), eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.createItem(itemDto, userId);

        assertEquals(expectedResponse, response);
        verify(itemClient).createItem(itemDto, userId);
    }

    @Test
    void getItemById_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemClient.getItemById(eq(itemId), eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.getItemById(itemId, userId);

        assertEquals(expectedResponse, response);
        verify(itemClient).getItemById(itemId, userId);
    }

    @Test
    void getAllItemsByOwner_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemClient.getAllItemsByOwner(eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.getAllItemsByOwner(userId);

        assertEquals(expectedResponse, response);
        verify(itemClient).getAllItemsByOwner(userId);
    }

    @Test
    void updateItem_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemClient.updateItem(eq(itemId), any(UpdateItemDto.class), eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.updateItem(itemId, updateItemDto, userId);

        assertEquals(expectedResponse, response);
        verify(itemClient).updateItem(itemId, updateItemDto, userId);
    }

    @Test
    void searchItems_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemClient.searchItems(eq("test")))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.searchItems("test");

        assertEquals(expectedResponse, response);
        verify(itemClient).searchItems("test");
    }

    @Test
    void addComment_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(itemClient.addComment(eq(itemId), eq(userId), any(CommentDto.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = itemController.addComment(itemId, userId, commentDto);

        assertEquals(expectedResponse, response);
        verify(itemClient).addComment(itemId, userId, commentDto);
    }
}
