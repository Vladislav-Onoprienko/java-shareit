package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingClient bookingClient;

    @InjectMocks
    private BookingController bookingController;

    private BookingDto bookingDto;
    private final Long userId = 1L;
    private final Long bookingId = 1L;

    @BeforeEach
    void setUp() {
        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    void createBooking_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(bookingClient.createBooking(any(BookingDto.class), eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.createBooking(bookingDto, userId);

        assertEquals(expectedResponse, response);
        verify(bookingClient).createBooking(bookingDto, userId);
    }

    @Test
    void approveBooking_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(bookingClient.approveBooking(eq(bookingId), eq(userId), eq(true)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.approveBooking(bookingId, true, userId);

        assertEquals(expectedResponse, response);
        verify(bookingClient).approveBooking(bookingId, userId, true);
    }

    @Test
    void getBooking_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(bookingClient.getBookingById(eq(bookingId), eq(userId)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.getBooking(bookingId, userId);

        assertEquals(expectedResponse, response);
        verify(bookingClient).getBookingById(bookingId, userId);
    }

    @Test
    void getUserBookings_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(bookingClient.getUserBookings(eq(userId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.getUserBookings("ALL", 0, 10, userId);

        assertEquals(expectedResponse, response);
        verify(bookingClient).getUserBookings(userId, "ALL", 0, 10);
    }

    @Test
    void getOwnerBookings_ShouldCallClient() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(bookingClient.getOwnerBookings(eq(userId), eq("ALL"), eq(0), eq(10)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = bookingController.getOwnerBookings("ALL", 0, 10, userId);

        assertEquals(expectedResponse, response);
        verify(bookingClient).getOwnerBookings(userId, "ALL", 0, 10);
    }
}
