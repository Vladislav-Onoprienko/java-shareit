package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRequestRepository itemRequestRepository;

    @Test
    void createItem_ShouldReturnCreatedItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto createdDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(itemService.createItem(any(ItemDto.class), anyLong())).thenReturn(createdDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).createItem(any(ItemDto.class), eq(1L));
    }

    @Test
    void getItemById_ShouldReturnItem() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .available(true)
                .build();

        when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemDto);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));

        verify(itemService).getItemById(1L, 1L);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItems() throws Exception {
        ItemDto item1 = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .build();

        ItemDto item2 = ItemDto.builder()
                .id(2L)
                .name("Hammer")
                .build();

        when(itemService.getAllItemsByOwner(anyLong())).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(itemService).getAllItemsByOwner(1L);
    }

    @Test
    void updateItem_ShouldReturnUpdatedItem() throws Exception {
        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("Updated Drill")
                .build();

        ItemDto updatedDto = ItemDto.builder()
                .id(1L)
                .name("Updated Drill")
                .available(true)
                .build();

        when(itemService.updateItem(anyLong(), any(UpdateItemDto.class), anyLong())).thenReturn(updatedDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Drill"));

        verify(itemService).updateItem(eq(1L), any(UpdateItemDto.class), eq(1L));
    }

    @Test
    void searchItems_ShouldReturnMatchingItems() throws Exception {
        ItemDto item1 = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .available(true)
                .build();

        when(itemService.searchItems(anyString())).thenReturn(List.of(item1));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(itemService).searchItems("drill");
    }

    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() throws Exception {
        when(itemService.searchItems(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemService).searchItems("");
    }

    @Test
    void addComment_ShouldReturnCreatedComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .text("Great item!")
                .build();

        UserDto authorDto = UserDto.builder()
                .id(1L)
                .name("John")
                .build();

        CommentDto createdComment = CommentDto.builder()
                .id(1L)
                .text("Great item!")
                .author(authorDto)
                .authorName("John")
                .created(LocalDateTime.now())
                .build();

        when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class))).thenReturn(createdComment);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"));

        verify(itemService).addComment(eq(1L), eq(1L), any(CommentDto.class));
    }

    @Test
    void getItemById_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(itemService.getItemById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Item not found"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());

        verify(itemService).getItemById(999L, 1L);
    }
}