package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto itemRequestDto) {
        log.info("Создание запроса пользователем ID: {}", userId);
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest itemRequest = itemRequestMapper.toEntity(itemRequestDto);
        itemRequest.setRequestor(requester);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос ID {} успешно создан пользователем ID {}", savedRequest.getId(), userId);

        return itemRequestMapper.toDto(savedRequest);
    }

    public List<ItemRequestDto> getByUser(Long userId) {
        log.info("Получение всех запросов пользователя ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdOrderByCreatedDesc(userId);
        return enrichWithItems(requests);
    }

    public List<ItemRequestDto> getAll(Long userId, int from, int size) {
        log.info("Получение всех запросов, созданных не пользователем ID: {}, from={}, size={}", userId, from, size);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        int pageNumber = from / size;
        PageRequest page = PageRequest.of(pageNumber, size);
        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId, page);
        return enrichWithItems(requests);
    }

    public ItemRequestDto getById(Long userId, Long requestId) {
        log.info("Получение запроса ID {} пользователем ID {}", requestId, userId);
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        ItemRequestDto responseDto = itemRequestMapper.toDto(itemRequest);
        responseDto.setItems(getItemsForRequest(requestId));
        return responseDto;
    }

    @Transactional
    public void setRequestForItem(Item item, Long requestId) {
        if (requestId != null) {
            ItemRequest request = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
            log.debug("Установлена связь с запросом ID: {}", requestId);
        }
    }

    private List<ItemRequestDto> enrichWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemRequestDto.ItemResponseDto>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds)
                .stream()
                .map(this::toItemResponseDto)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ItemRequestDto.ItemResponseDto::getRequestId));

        List<ItemRequestDto> result = requests.stream()
                .map(itemRequestMapper::toDto)
                .collect(Collectors.toList());

        result.forEach(dto ->
                dto.setItems(itemsByRequestId.getOrDefault(dto.getId(), Collections.emptyList())));

        return result;
    }

    private List<ItemRequestDto.ItemResponseDto> getItemsForRequest(Long requestId) {
        return itemRepository.findByRequestId(requestId)
                .stream()
                .map(this::toItemResponseDto)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private ItemRequestDto.ItemResponseDto toItemResponseDto(Item item) {
        if (item.getName() == null) {
            log.warn("Item ID {} имеет null name!", item.getId());
        }
        if (item.getOwner() == null) {
            log.warn("Item ID {} имеет null owner!", item.getId());
        }

        return ItemRequestDto.ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName() != null ? item.getName() : "Default Name")
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }
}
