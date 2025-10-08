package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void createItem_ShouldCreateItem() {
        Long ownerId = 1L;
        User owner = new User(ownerId, "Alex", "alex@email.com");
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();
        Item item = new Item();
        item.setName("Drill");
        item.setOwner(owner);

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemMapper.toEntity(itemDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder()
                .id(1L)
                .name("Drill")
                .build());

        ItemDto result = itemService.createItem(itemDto, ownerId);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void createItem_ShouldThrowWhenUserNotFound() {
        Long ownerId = 999L;
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                itemService.createItem(itemDto, ownerId));

        verify(userRepository).findById(ownerId);
        verify(itemRepository, never()).save(any());
    }

    @Test
    void updateItem_ShouldUpdateItem() {
        Long itemId = 1L;
        Long ownerId = 1L;
        User owner = new User(ownerId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);
        item.setName("Old Name");

        UpdateItemDto updateDto = UpdateItemDto.builder()
                .name("New Name")
                .build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder()
                .id(itemId)
                .name("New Name")
                .build());

        ItemDto result = itemService.updateItem(itemId, updateDto, ownerId);

        assertEquals("New Name", result.getName());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldThrowWhenNotOwner() {
        Long itemId = 1L;
        Long ownerId = 2L;
        User owner = new User(1L, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        UpdateItemDto updateDto = UpdateItemDto.builder().name("New Name").build();

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ForbiddenException.class, () ->
                itemService.updateItem(itemId, updateDto, ownerId));
    }

    @Test
    void getItemById_ShouldReturnItem() {
        Long itemId = 1L;
        Long userId = 1L;
        User owner = new User(userId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder().id(itemId).build());
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(itemId, userId);

        assertNotNull(result);
        assertEquals(itemId, result.getId());
    }

    @Test
    void getItemById_ShouldThrowWhenItemNotFound() {
        Long itemId = 999L;
        Long userId = 1L;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () ->
                itemService.getItemById(itemId, userId));

        verify(itemRepository).findById(itemId);
    }

    @Test
    void getAllItemsByOwner_ShouldReturnEmptyListWhenUserNotFound() {
        Long ownerId = 999L;

        when(userRepository.existsById(ownerId)).thenReturn(false);

        List<ItemDto> result = itemService.getAllItemsByOwner(ownerId);

        assertTrue(result.isEmpty());
        verify(userRepository).existsById(ownerId);
        verify(itemRepository, never()).findByOwnerIdOrderByIdAsc(any());
    }

    @Test
    void getAllItemsByOwner_ShouldReturnItemsWhenUserExists() {
        Long ownerId = 1L;
        User owner = new User(ownerId, "Alex", "alex@email.com");
        Item item1 = new Item();
        item1.setId(1L);
        item1.setOwner(owner);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setOwner(owner);

        when(userRepository.existsById(ownerId)).thenReturn(true);
        when(itemRepository.findByOwnerIdOrderByIdAsc(ownerId)).thenReturn(List.of(item1, item2));
        when(itemMapper.toDtoList(anyList())).thenReturn(List.of(
                ItemDto.builder().id(1L).build(),
                ItemDto.builder().id(2L).build()
        ));

        List<ItemDto> result = itemService.getAllItemsByOwner(ownerId);

        assertEquals(2, result.size());
        verify(userRepository).existsById(ownerId);
        verify(itemRepository).findByOwnerIdOrderByIdAsc(ownerId);
    }

    @Test
    void getItemById_ShouldIncludeComments() {
        Long itemId = 1L;
        Long userId = 1L;
        User owner = new User(userId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Great item!");
        comment.setItem(item);
        comment.setAuthor(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder().id(itemId).build());
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of(comment));
        when(commentMapper.toDto(comment)).thenReturn(CommentDto.builder().id(1L).text("Great item!").build());

        ItemDto result = itemService.getItemById(itemId, userId);

        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals("Great item!", result.getComments().get(0).getText());
    }

    @Test
    void getItemById_ShouldSetBookingsForOwner() {
        Long itemId = 1L;
        Long userId = 1L;
        User owner = new User(userId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        Booking lastBooking = new Booking();
        lastBooking.setId(1L);
        lastBooking.setBooker(owner);

        Booking nextBooking = new Booking();
        nextBooking.setId(2L);
        nextBooking.setBooker(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder().id(itemId).build());
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of());
        when(bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                eq(itemId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(lastBooking);
        when(bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(itemId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(nextBooking);

        ItemDto result = itemService.getItemById(itemId, userId);

        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
        verify(bookingRepository).findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                eq(itemId), any(LocalDateTime.class), eq(BookingStatus.APPROVED));
        verify(bookingRepository).findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                eq(itemId), any(LocalDateTime.class), eq(BookingStatus.APPROVED));
    }

    @Test
    void getItemById_ShouldNotSetBookingsForNonOwner() {
        Long itemId = 1L;
        Long ownerId = 1L;
        Long userId = 2L;
        User owner = new User(ownerId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setOwner(owner);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(ItemDto.builder().id(itemId).build());
        when(commentRepository.findByItemId(itemId)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(itemId, userId);

        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(bookingRepository, never()).findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(any(), any(), any());
        verify(bookingRepository, never()).findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(any(), any(), any());
    }

    @Test
    void deleteItem_ShouldDeleteWhenItemExists() {
        Long itemId = 1L;

        when(itemRepository.existsById(itemId)).thenReturn(true);
        doNothing().when(itemRepository).deleteById(itemId);

        assertDoesNotThrow(() -> itemService.deleteItem(itemId));

        verify(itemRepository).existsById(itemId);
        verify(itemRepository).deleteById(itemId);
    }

    @Test
    void deleteItem_ShouldThrowWhenItemNotFound() {
        Long itemId = 999L;

        when(itemRepository.existsById(itemId)).thenReturn(false);

        assertThrows(ItemNotFoundException.class, () ->
                itemService.deleteItem(itemId));

        verify(itemRepository).existsById(itemId);
        verify(itemRepository, never()).deleteById(any());
    }

    @Test
    void searchItems_ShouldReturnEmptyListWhenTextIsBlank() {
        List<ItemDto> result = itemService.searchItems("   ");

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(any());
    }

    @Test
    void searchItems_ShouldReturnEmptyListWhenTextIsNull() {
        List<ItemDto> result = itemService.searchItems(null);

        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(any());
    }

    @Test
    void searchItems_ShouldReturnItemsWhenTextProvided() {
        String searchText = "drill";
        User owner = new User(1L, "Alex", "alex@email.com");
        Item item1 = new Item();
        item1.setId(1L);
        item1.setName("Power Drill");
        item1.setAvailable(true);
        item1.setOwner(owner);
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Hand Drill");
        item2.setAvailable(true);
        item2.setOwner(owner);

        when(itemRepository.searchAvailableItems(searchText.toLowerCase())).thenReturn(List.of(item1, item2));
        when(itemMapper.toDto(item1)).thenReturn(ItemDto.builder().id(1L).name("Power Drill").build());
        when(itemMapper.toDto(item2)).thenReturn(ItemDto.builder().id(2L).name("Hand Drill").build());

        List<ItemDto> result = itemService.searchItems(searchText);

        assertEquals(2, result.size());
        verify(itemRepository).searchAvailableItems(searchText.toLowerCase());
    }

    @Test
    void searchItems_ShouldFilterOutUnavailableItems() {
        String searchText = "drill";
        User owner = new User(1L, "Alex", "alex@email.com");

        Item availableItem = new Item();
        availableItem.setId(1L);
        availableItem.setName("Power Drill");
        availableItem.setAvailable(true);
        availableItem.setOwner(owner);

        Item unavailableItem = new Item();
        unavailableItem.setId(2L);
        unavailableItem.setName("Hand Drill");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner);

        when(itemRepository.searchAvailableItems(searchText.toLowerCase()))
                .thenReturn(List.of(availableItem, unavailableItem));

        when(itemMapper.toDto(availableItem))
                .thenReturn(ItemDto.builder().id(1L).name("Power Drill").build());

        List<ItemDto> result = itemService.searchItems(searchText);

        assertEquals(1, result.size());
        assertEquals("Power Drill", result.get(0).getName());
        verify(itemRepository).searchAvailableItems(searchText.toLowerCase());

        verify(itemMapper).toDto(availableItem);
        verify(itemMapper, never()).toDto(unavailableItem);
    }

    @Test
    void addComment_ShouldAddComment() {
        Long itemId = 1L;
        Long userId = 1L;
        User author = new User(userId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        CommentDto commentDto = CommentDto.builder().text("Great!").build();
        Comment comment = new Comment();
        comment.setText("Great!");

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedBookingsForItemAndUser(
                eq(itemId), eq(userId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of(new Booking()));
        when(commentMapper.toEntity(commentDto, author, item)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toDto(comment)).thenReturn(CommentDto.builder().id(1L).text("Great!").build());

        CommentDto result = itemService.addComment(itemId, userId, commentDto);

        assertNotNull(result);
        assertEquals("Great!", result.getText());
    }

    @Test
    void addComment_ShouldThrowWhenNoBooking() {
        Long itemId = 1L;
        Long userId = 1L;
        User author = new User(userId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        CommentDto commentDto = CommentDto.builder().text("Great!").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(author));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingRepository.findPastApprovedBookingsForItemAndUser(
                eq(itemId), eq(userId), any(LocalDateTime.class), eq(BookingStatus.APPROVED)))
                .thenReturn(List.of());

        assertThrows(ValidationException.class, () ->
                itemService.addComment(itemId, userId, commentDto));
    }
}
