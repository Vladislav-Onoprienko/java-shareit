package ru.practicum.shareit.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "requestId", source = "request.id")
    ItemDto toDto(Item item);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    Item toEntity(ItemDto itemDto);

    List<ItemDto> toDtoList(List<Item> items);
}
