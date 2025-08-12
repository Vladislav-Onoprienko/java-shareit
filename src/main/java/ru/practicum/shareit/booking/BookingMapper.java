package ru.practicum.shareit.booking;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookingMapper {
    public Booking toEntity(BookingDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }

        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }


    public BookingResponseDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        UserDto bookerDto = booking.getBooker() != null ? UserDto.builder()
                .id(booking.getBooker().getId())
                .name(booking.getBooker().getName())
                .build() : null;

        ItemDto itemDto = booking.getItem() != null ? ItemDto.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .build() : null;

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(bookerDto)
                .item(itemDto)
                .build();
    }

    public List<BookingResponseDto> toDtoList(List<Booking> bookings) {
        return bookings.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
