package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnavailableItemException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingDto bookingDto, Long bookerId) {
        log.info("Создание бронирования для вещи ID {} пользователем ID {}", bookingDto.getItemId(), bookerId);
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> {
                    log.error("При создании бронирования пользователь с ID {} не найден", bookerId);
                    return new NotFoundException("Пользователь не найден");
                });
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> {
                    log.error("Вещь с ID {} не найдена", bookingDto.getItemId());
                    return new NotFoundException("Вещь не найдена");
                });

        if (!item.getAvailable()) {
            log.warn("Попытка забронировать недоступную вещь ID {}", item.getId());
            throw new UnavailableItemException("Вещь недоступна для бронирования");
        }
        if (bookerId.equals(item.getOwner().getId())) {
            log.warn("Попытка владельца ID {} забронировать свою вещь ID {}", bookerId, item.getId());
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }
        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            log.warn("Некорректные даты бронирования: окончание {} раньше начала {}",
                    bookingDto.getEnd(), bookingDto.getStart());
            throw new ConflictException("Дата окончания бронирования не может быть раньше даты начала");
        }

        Booking booking = bookingMapper.toEntity(bookingDto);
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование ID {} успешно создано для вещи ID {}", savedBooking.getId(), item.getId());

        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(Long bookingId, Long ownerId, boolean approved) {
        log.info("{} бронирования ID {} владельцем ID {}",
                approved ? "Подтверждение" : "Отклонение", bookingId, ownerId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("Бронирование с ID {} не найдено", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("Попытка обработки бронирования ID {} не владельцем ID {}", bookingId, ownerId);
            throw new ForbiddenException("Подтверждать бронирование может только владелец");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            log.warn("Попытка повторной обработки бронирования ID {}", bookingId);
            throw new ConflictException("Бронирование уже было обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Бронирование ID {} обновлено со статусом {}", updatedBooking.getId(), updatedBooking.getStatus());

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        log.debug("Запрос бронирования ID {} пользователем ID {}", bookingId, userId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.error("При запросе бронирования пользователя, бронирование с ID {} не найдено", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("Попытка просмотра бронирования ID {} неавторизованным пользователем ID {}",
                    bookingId, userId);
            throw new NotFoundException("Просматривать бронирование может только автор или владелец");
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(Long bookerId, String state, int from, int size) {
        log.info("Получение бронирований пользователя ID {} в статусе {}, from={}, size={}",
                bookerId, state, from, size);

        if (from < 0 || size <= 0) {
            log.warn("Некорректные параметры пагинации: from={}, size={}", from, size);
            throw new ValidationException("Неверные параметры пагинации");
        }

        if (!state.equalsIgnoreCase("ALL")) {
            try {
                BookingStatus.valueOf(state.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Неизвестный статус бронирования: {}", state);
                throw new ValidationException("Неизвестное состояние " + state);
            }
        }

        userRepository.findById(bookerId)
                .orElseThrow(() -> {
                    log.error("При получении бронирований пользователь с ID {} не найден", bookerId);
                    return new NotFoundException("Пользователь не найден");
                });

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        return switch (state.toUpperCase()) {
            case "CURRENT" -> bookingMapper.toDtoList(bookingRepository
                    .findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now, page));
            case "PAST" -> bookingMapper.toDtoList(bookingRepository
                    .findByBookerIdAndEndBeforeOrderByStartDesc(bookerId, now, page));
            case "FUTURE" -> bookingMapper.toDtoList(bookingRepository
                    .findByBookerIdAndStartAfterOrderByStartDesc(bookerId, now, page));
            case "WAITING", "REJECTED" -> bookingMapper.toDtoList(bookingRepository
                    .findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.valueOf(state.toUpperCase()), page));
            default -> bookingMapper.toDtoList(bookingRepository
                    .findByBookerIdOrderByStartDesc(bookerId, page));
        };
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(Long ownerId, String state, int from, int size) {
        log.info("Получение бронирований владельца ID {} в статусе {}, from={}, size={}",
                ownerId, state, from, size);

        userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Владелец с ID {} не найден", ownerId);
                    return new NotFoundException("Пользователь не найден");
                });

        PageRequest page = PageRequest.of(from / size, size);
        LocalDateTime now = LocalDateTime.now();

        return switch (state.toUpperCase()) {
            case "CURRENT" -> bookingMapper.toDtoList(bookingRepository
                    .findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(ownerId, now, now, page));
            case "PAST" -> bookingMapper.toDtoList(bookingRepository
                    .findByItemOwnerIdAndEndBeforeOrderByStartDesc(ownerId, now, page));
            case "FUTURE" -> bookingMapper.toDtoList(bookingRepository
                    .findByItemOwnerIdAndStartAfterOrderByStartDesc(ownerId, now, page));
            case "WAITING", "REJECTED" -> bookingMapper.toDtoList(bookingRepository
                    .findByItemOwnerIdAndStatusOrderByStartDesc(ownerId, BookingStatus.valueOf(state.toUpperCase()), page));
            default -> bookingMapper.toDtoList(bookingRepository
                    .findByItemOwnerIdOrderByStartDesc(ownerId, page));
        };
    }
}