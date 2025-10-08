package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void createBooking_ShouldCreateBooking() {
        Long bookerId = 1L;
        Long itemId = 1L;
        User booker = new User(bookerId, "Alex", "alex@email.com");
        User owner = new User(2L, "Owner", "owner@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Booking booking = new Booking();
        booking.setId(1L);

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(bookingMapper.toEntity(bookingDto)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(BookingResponseDto.builder().id(1L).build());

        BookingResponseDto result = bookingService.createBooking(bookingDto, bookerId);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createBooking_ShouldThrowWhenItemUnavailable() {
        Long bookerId = 1L;
        Long itemId = 1L;
        User booker = new User(bookerId, "Alex", "alex@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(false);

        BookingDto bookingDto = BookingDto.builder().itemId(itemId).build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(UnavailableItemException.class, () ->
                bookingService.createBooking(bookingDto, bookerId));
    }

    @Test
    void createBooking_ShouldThrowWhenOwnerBooksOwnItem() {
        Long ownerId = 1L;
        Long itemId = 1L;
        User owner = new User(ownerId, "Owner", "owner@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () ->
                bookingService.createBooking(bookingDto, ownerId));
    }

    @Test
    void createBooking_ShouldThrowWhenEndBeforeStart() {
        Long bookerId = 1L;
        Long itemId = 1L;
        User booker = new User(bookerId, "Alex", "alex@email.com");
        User owner = new User(2L, "Owner", "owner@email.com");
        Item item = new Item();
        item.setId(itemId);
        item.setAvailable(true);
        item.setOwner(owner);

        BookingDto bookingDto = BookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        when(userRepository.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThrows(ConflictException.class, () ->
                bookingService.createBooking(bookingDto, bookerId));
    }

    @Test
    void approveBooking_ShouldApproveBooking() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        User owner = new User(ownerId, "Owner", "owner@email.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(BookingResponseDto.builder().id(bookingId).build());

        BookingResponseDto result = bookingService.approveBooking(bookingId, ownerId, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
    }

    @Test
    void approveBooking_ShouldRejectBooking() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        User owner = new User(ownerId, "Owner", "owner@email.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(BookingResponseDto.builder().id(bookingId).build());

        BookingResponseDto result = bookingService.approveBooking(bookingId, ownerId, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
    }

    @Test
    void approveBooking_ShouldThrowWhenNotOwner() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        Long notOwnerId = 2L;
        User owner = new User(ownerId, "Owner", "owner@email.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.WAITING);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ForbiddenException.class, () ->
                bookingService.approveBooking(bookingId, notOwnerId, true));
    }

    @Test
    void approveBooking_ShouldThrowWhenAlreadyProcessed() {
        Long bookingId = 1L;
        Long ownerId = 1L;
        User owner = new User(ownerId, "Owner", "owner@email.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ConflictException.class, () ->
                bookingService.approveBooking(bookingId, ownerId, true));
    }

    @Test
    void getBookingById_ShouldReturnBooking() {
        Long bookingId = 1L;
        Long userId = 1L;
        User user = new User(userId, "User", "user@email.com");
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(user);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(BookingResponseDto.builder().id(bookingId).build());

        BookingResponseDto result = bookingService.getBookingById(bookingId, userId);

        assertNotNull(result);
        assertEquals(bookingId, result.getId());
    }

    @Test
    void getBookingById_ShouldThrowWhenUnauthorized() {
        Long bookingId = 1L;
        Long authorizedUserId = 1L;
        Long unauthorizedUserId = 2L;
        User booker = new User(authorizedUserId, "Booker", "booker@email.com");
        User owner = new User(3L, "Owner", "owner@email.com");
        Item item = new Item();
        item.setOwner(owner);
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setBooker(booker);
        booking.setItem(item);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () ->
                bookingService.getBookingById(bookingId, unauthorizedUserId));
    }

    @Test
    void getUserBookings_ShouldValidatePagination() {
        Long userId = 1L;
        int from = -1;
        int size = 10;

        assertThrows(ValidationException.class, () ->
                bookingService.getUserBookings(userId, "ALL", from, size));
    }

    @Test
    void getUserBookings_ShouldHandleDifferentStates() {
        Long userId = 1L;
        int from = 0;
        int size = 10;
        User user = new User(userId, "Test User", "test@email.com");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> bookingService.getUserBookings(userId, "WAITING", from, size));
        assertDoesNotThrow(() -> bookingService.getUserBookings(userId, "APPROVED", from, size));
        assertDoesNotThrow(() -> bookingService.getUserBookings(userId, "REJECTED", from, size));
        assertDoesNotThrow(() -> bookingService.getUserBookings(userId, "CANCELED", from, size));

        assertDoesNotThrow(() -> bookingService.getUserBookings(userId, "ALL", from, size));

        assertThrows(ValidationException.class, () ->
                bookingService.getUserBookings(userId, "UNKNOWN", from, size));
    }

    @Test
    void getOwnerBookings_ShouldReturnAllBookings() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(ownerId), any(PageRequest.class)))
                .thenReturn(List.of(new Booking(), new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(
                BookingResponseDto.builder().id(1L).build(),
                BookingResponseDto.builder().id(2L).build()
        ));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "ALL", from, size);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(eq(ownerId), any(PageRequest.class));
    }

    @Test
    void getOwnerBookings_ShouldReturnCurrentBookings() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                eq(ownerId), any(LocalDateTime.class), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "CURRENT", from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_ShouldReturnPastBookings() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(
                eq(ownerId), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "PAST", from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_ShouldReturnFutureBookings() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(
                eq(ownerId), any(LocalDateTime.class), any(PageRequest.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "FUTURE", from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_ShouldReturnWaitingBookings() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                eq(ownerId), eq(BookingStatus.WAITING), any(PageRequest.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "WAITING", from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_ShouldReturnRejectedBookings() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                eq(ownerId), eq(BookingStatus.REJECTED), any(PageRequest.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "REJECTED", from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getOwnerBookings_ShouldValidatePagination() {
        Long ownerId = 1L;
        int from = -1;
        int size = 10;

        assertThrows(NotFoundException.class, () ->
                bookingService.getOwnerBookings(ownerId, "ALL", from, size));
    }

    @Test
    void getOwnerBookings_ShouldHandleUnknownState() {
        Long ownerId = 1L;
        int from = 0;
        int size = 10;
        User owner = new User(ownerId, "Owner", "owner@email.com");

        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerIdOrderByStartDesc(eq(ownerId), any(PageRequest.class)))
                .thenReturn(List.of(new Booking()));
        when(bookingMapper.toDtoList(anyList())).thenReturn(List.of(BookingResponseDto.builder().id(1L).build()));

        List<BookingResponseDto> result = bookingService.getOwnerBookings(ownerId, "UNKNOWN", from, size);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByItemOwnerIdOrderByStartDesc(eq(ownerId), any(PageRequest.class));
    }
}
