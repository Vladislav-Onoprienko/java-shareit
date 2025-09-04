package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;

@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(
            @Valid @RequestBody BookingDto bookingDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return bookingClient.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @PathVariable @Positive Long bookingId,
            @RequestParam boolean approved,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return bookingClient.approveBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(
            @PathVariable @Positive Long bookingId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return bookingClient.getBookingById(bookingId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getUserBookings(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        validateBookingState(state);
        return bookingClient.getUserBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getOwnerBookings(
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {

        validateBookingState(state);
        return bookingClient.getOwnerBookings(userId, state, from, size);
    }

    private void validateBookingState(String state) {
        if (!state.equals("ALL") && !state.equals("CURRENT") &&
                !state.equals("PAST") && !state.equals("FUTURE") &&
                !state.equals("WAITING") && !state.equals("REJECTED")) {
            throw new IllegalArgumentException("Unknown state: " + state);
        }
    }
}
