package ru.practicum.shareit.item;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item,Long> {
    @NonNull
    List<Item> findByOwnerIdOrderByIdAsc(@NonNull Long ownerId);

    boolean existsById(@NonNull Long id);

    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    @NonNull
    List<Item> searchAvailableItems(@Param("text") String text);
}
