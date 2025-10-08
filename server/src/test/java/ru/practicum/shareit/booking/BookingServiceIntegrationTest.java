package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createAndGetBooking_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Alice");
        booker.setEmail("alice@email.com");
        User savedBooker = userRepository.save(booker);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, savedOwner.getId());

        BookingDto bookingDto = BookingDto.builder()
                .itemId(createdItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingResponseDto createdBooking = bookingService.createBooking(bookingDto, savedBooker.getId());
        assertNotNull(createdBooking.getId());

        BookingResponseDto foundBooking = bookingService.getBookingById(createdBooking.getId(), savedBooker.getId());
        assertEquals(createdBooking.getId(), foundBooking.getId());
    }

    @Test
    void createBooking_ShouldThrowWhenItemUnavailable() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Alice");
        booker.setEmail("alice@email.com");
        User savedBooker = userRepository.save(booker);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(false)
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, savedOwner.getId());

        BookingDto bookingDto = BookingDto.builder()
                .itemId(createdItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(UnavailableItemException.class, () ->
                bookingService.createBooking(bookingDto, savedBooker.getId()));
    }

    @Test
    void getUserBookings_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Alice");
        booker.setEmail("alice@email.com");
        User savedBooker = userRepository.save(booker);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, savedOwner.getId());

        BookingDto bookingDto = BookingDto.builder()
                .itemId(createdItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.createBooking(bookingDto, savedBooker.getId());

        List<BookingResponseDto> bookings = bookingService.getUserBookings(savedBooker.getId(), "ALL", 0, 10);
        assertFalse(bookings.isEmpty());
    }

    @Test
    void getOwnerBookings_ShouldWork() {
        User owner = new User();
        owner.setName("Alex");
        owner.setEmail("alex@email.com");
        User savedOwner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Alice");
        booker.setEmail("alice@email.com");
        User savedBooker = userRepository.save(booker);

        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto createdItem = itemService.createItem(itemDto, savedOwner.getId());

        BookingDto bookingDto = BookingDto.builder()
                .itemId(createdItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        bookingService.createBooking(bookingDto, savedBooker.getId());

        List<BookingResponseDto> bookings = bookingService.getOwnerBookings(savedOwner.getId(), "ALL", 0, 10);
        assertFalse(bookings.isEmpty());
    }
}
