package ru.practicum.shareit.item;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ItemNotFoundException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
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
    private final BookingRepository bookingRepository;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

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

    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи ID {} пользователем ID {}", itemId, userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещь не найдена"));

        ItemDto itemDto = itemMapper.toDto(item);

        List<Comment> comments = commentRepository.findByItemId(itemId);
        log.debug("Найдено {} комментариев для вещи ID {}", comments.size(), itemId);
        itemDto.setComments(comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList()));

        if (item.getOwner().getId().equals(userId)) {
            log.debug("Пользователь ID {} является владельцем вещи ID {}", userId, itemId);
            LocalDateTime now = LocalDateTime.now();
            itemDto.setLastBooking(toBookingShortDto(
                    bookingRepository.findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
                            itemId, now, BookingStatus.APPROVED)));
            itemDto.setNextBooking(toBookingShortDto(
                    bookingRepository.findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, now, BookingStatus.APPROVED)));
        }

        log.info("Успешно возвращена вещь ID {}", itemId);
        return itemDto;
    }

    List<ItemDto> getAllItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей владельца ID {}", ownerId);
        if (!userRepository.existsById(ownerId)) {
            log.warn("Владелец с ID {} не найден, возвращен пустой список", ownerId);
            return Collections.emptyList();
        }

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(ownerId);

        if (items.isEmpty()) {
            log.debug("У владельца ID {} не найдено вещей", ownerId);
            return Collections.emptyList();
        }

        log.info("Найдено {} вещей владельца ID {}", items.size(), ownerId);
        return itemMapper.toDtoList(items);
    }

    public void deleteItem(Long itemId) {
        log.info("Удаление вещи с ID: {}", itemId);
        if (!itemRepository.existsById(itemId)) {
            log.error("Попытка удаления несуществующей вещи с ID: {}", itemId);
            throw new ItemNotFoundException("Вещь не найдена");
        }
        itemRepository.deleteById(itemId);
    }

    List<ItemDto> searchItems(String text) {
        log.info("Поиск доступных вещей по запросу: '{}'", text);
        if (text == null || text.isBlank()) {
            log.debug("Пустой поисковый запрос, возвращен пустой список");
            return Collections.emptyList();
        }

        String searchText = text.toLowerCase();
        List<Item> items = itemRepository.searchAvailableItems(searchText);
        log.debug("Найдено {} вещей по запросу '{}'", items.size(), text);

        List<ItemDto> result = items.stream()
                .filter(Item::getAvailable)
                .map(itemMapper::toDto)
                .collect(Collectors.toList());

        log.info("Возвращено {} доступных вещей по запросу '{}'", result.size(), text);
        return result;
    }

    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Добавление комментария к вещи ID {} пользователем ID {}", itemId, userId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        List<Booking> pastBookings = bookingRepository.findPastApprovedBookingsForItemAndUser(
                itemId,
                userId,
                LocalDateTime.now(),
                BookingStatus.APPROVED);

        if (pastBookings.isEmpty()) {
            log.warn("Попытка оставить комментарий к неарендованной вещи. Вещь ID: {}," +
                    " пользователь ID: {}", itemId, userId);
            throw new ValidationException("Нельзя оставить комментарий к неарендованной вещи");
        }

        Comment comment = commentMapper.toEntity(commentDto, author, item);
        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий ID {} успешно добавлен к вещи ID {}", savedComment.getId(), itemId);

        return commentMapper.toDto(savedComment);
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .build();
    }
}
