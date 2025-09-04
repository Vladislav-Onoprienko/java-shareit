package ru.practicum.shareit.booking;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long bookerId, LocalDateTime start,
                                                                          LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :bookerId " +
            "AND b.end < :now " +
            "AND b.status = :status")
    List<Booking> findPastApprovedBookingsForItemAndUser(
            @Param("itemId") Long itemId,
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now,
            @Param("status") BookingStatus status);

    Booking findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(Long itemId, LocalDateTime now,
                                                                     BookingStatus status);

    Booking findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(Long itemId, LocalDateTime now,
                                                                   BookingStatus status);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status, Pageable pageable);
}
