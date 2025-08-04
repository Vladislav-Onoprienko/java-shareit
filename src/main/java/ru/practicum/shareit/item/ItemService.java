package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    public ItemDto createItem(ItemDto itemDto, Long ownerId) {
        log.info("Создание вещи '{}' для пользователя с ID: {}", itemDto.getName(), ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Владелец с ID {} не найден", ownerId);
                    return new NotFoundException("Пользователь не найден");
                });

        Item item = itemMapper.toEntity(itemDto);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    public ItemDto updateItem(Long itemId, UpdateItemDto updateItemDto, Long ownerId) {
        log.info("Обновление вещи с ID: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Вещь с ID {} не найдена", itemId);
                    return new NotFoundException("Вещь не найдена");
                });

        if (!ownerId.equals(item.getOwner().getId())) {
            log.warn("Попытка обновления чужой вещи. Вещь ID: {}, пользователь ID: {}", itemId, ownerId);
            throw new ForbiddenException("Нельзя редактировать чужую вещь");
        }

        if (updateItemDto.getName() != null) item.setName(updateItemDto.getName());
        if (updateItemDto.getDescription() != null) item.setDescription(updateItemDto.getDescription());
        if (updateItemDto.getAvailable() != null) item.setAvailable(updateItemDto.getAvailable());

        Item updatedItem = itemRepository.save(item);
        return itemMapper.toDto(updatedItem);
    }

    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));
        return itemMapper.toDto(item);
    }

    List<ItemDto> getAllItemsByOwner(Long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteItem(Long itemId) {
        log.info("Удаление вещи с ID: {}", itemId);
        if (!itemRepository.deleteById(itemId)) {
            log.error("Попытка удаления несуществующей вещи с ID: {}", itemId);
            throw new ItemNotFoundException("Вещь не найдена");
        }
    }

    List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}
