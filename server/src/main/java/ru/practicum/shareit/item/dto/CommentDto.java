package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    private Long id;

    private String text;

    private UserDto author;

    private String authorName;

    private LocalDateTime created;
}
