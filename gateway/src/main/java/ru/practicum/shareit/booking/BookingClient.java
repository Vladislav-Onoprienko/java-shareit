package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.Map;

@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createBooking(BookingDto bookingDto, Long userId) {
        return post(API_PREFIX, userId, bookingDto);
    }

    public ResponseEntity<Object> approveBooking(Long bookingId, Long userId, boolean approved) {
        Map<String, Object> parameters = Map.of("bookingId", bookingId);
        return patch(API_PREFIX + "/{bookingId}?approved=" + approved, userId, parameters, null);
    }

    public ResponseEntity<Object> getBookingById(Long bookingId, Long userId) {
        Map<String, Object> parameters = Map.of("bookingId", bookingId);
        return get(API_PREFIX + "/{bookingId}", userId, parameters);
    }

    public ResponseEntity<Object> getUserBookings(Long userId, String state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get(API_PREFIX + "?state={state}&from={from}&size={size}", userId, parameters);
    }

    public ResponseEntity<Object> getOwnerBookings(Long userId, String state, int from, int size) {
        Map<String, Object> parameters = Map.of(
                "state", state,
                "from", from,
                "size", size
        );
        return get(API_PREFIX + "/owner?state={state}&from={from}&size={size}", userId, parameters);
    }
}
