package ru.practicum.shareit.request;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requestor", ignore = true)
    @Mapping(target = "created", ignore = true)
    ItemRequest toEntity(ItemRequestDto itemRequestDto);

    @Mapping(target = "items", ignore = true)
    ItemRequestDto toDto(ItemRequest itemRequest);

    List<ItemRequestDto> toDtoList(List<ItemRequest> itemRequests);
}