package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        imports = {LocalDateTime.class}
)
public interface CommentMapper {
    @Mapping(target = "authorName", source = "author.name")
    CommentDto toDto(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "created", expression = "java(LocalDateTime.now())")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "item", source = "item")
    Comment toEntity(CommentDto commentDto, User author, Item item);

    default UserDto mapUser(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail());
    }
}