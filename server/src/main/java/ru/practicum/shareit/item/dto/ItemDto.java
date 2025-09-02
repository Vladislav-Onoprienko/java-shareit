package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ItemDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long ownerId;

    private Long requestId;

    private BookingShortDto lastBooking;

    private BookingShortDto nextBooking;

    private List<CommentDto> comments = new ArrayList<>();
}
