package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(
            @Valid @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemRequestService.create(userId, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getByUser(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemRequestService.getByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAll(
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {
        return itemRequestService.getAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(
            @PathVariable @Positive Long requestId,
            @RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        ItemRequestDto result = itemRequestService.getById(userId, requestId);

        if (!result.getItems().isEmpty()) {
            ItemRequestDto.ItemResponseDto firstItem = result.getItems().getFirst();
        }

        return result;
    }
}
