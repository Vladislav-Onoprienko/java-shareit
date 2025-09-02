package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.UpdateItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(ItemDto itemDto, Long ownerId) {
        return post(API_PREFIX, ownerId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Long itemId, Long userId) {
        Map<String, Object> parameters = Map.of("itemId", itemId);
        return get(API_PREFIX + "/{itemId}", userId, parameters);
    }

    public ResponseEntity<Object> getAllItemsByOwner(Long ownerId) {
        return get(API_PREFIX, ownerId);
    }

    public ResponseEntity<Object> updateItem(Long itemId, UpdateItemDto updateItemDto, Long ownerId) {
        Map<String, Object> parameters = Map.of("itemId", itemId);
        return patch(API_PREFIX + "/{itemId}", ownerId, parameters, updateItemDto);
    }

    public ResponseEntity<Object> searchItems(String text) {
        Map<String, Object> parameters = Map.of("text", text);
        return get(API_PREFIX + "/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> addComment(Long itemId, Long userId, CommentDto commentDto) {
        Map<String, Object> parameters = Map.of("itemId", itemId);
        return post(API_PREFIX + "/{itemId}/comment", userId, parameters, commentDto);
    }
}
