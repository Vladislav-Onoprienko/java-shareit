package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    @MockBean
    private BookingRepository bookingRepository;

    @MockBean
    private ItemRepository itemRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ItemRequestRepository itemRequestRepository;

    @MockBean
    private CommentRepository commentRepository;

    @Test
    void createBooking_ShouldReturnCreatedBooking() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.WAITING);

        when(bookingService.createBooking(any(BookingDto.class), anyLong())).thenReturn(responseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));

        verify(bookingService).createBooking(any(BookingDto.class), eq(1L));
    }

    @Test
    void approveBooking_ShouldReturnApprovedBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean())).thenReturn(responseDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).approveBooking(1L, 1L, true);
    }

    @Test
    void getBooking_ShouldReturnBooking() throws Exception {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(1L);
        responseDto.setStatus(BookingStatus.APPROVED);

        when(bookingService.getBookingById(anyLong(), anyLong())).thenReturn(responseDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(bookingService).getBookingById(1L, 1L);
    }

    @Test
    void getUserBookings_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.APPROVED);

        BookingResponseDto booking2 = new BookingResponseDto();
        booking2.setId(2L);
        booking2.setStatus(BookingStatus.WAITING);

        when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking1, booking2));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].status").value("WAITING"));

        verify(bookingService).getUserBookings(1L, "ALL", 0, 10);
    }

    @Test
    void getOwnerBookings_ShouldReturnBookings() throws Exception {
        BookingResponseDto booking1 = new BookingResponseDto();
        booking1.setId(1L);
        booking1.setStatus(BookingStatus.APPROVED);

        when(bookingService.getOwnerBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(booking1));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].status").value("APPROVED"));

        verify(bookingService).getOwnerBookings(1L, "ALL", 0, 5);
    }

    @Test
    void getBooking_WhenNotFound_ShouldReturnNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());

        verify(bookingService).getBookingById(999L, 1L);
    }

    @Test
    void getUserBookings_WithDefaultParams_ShouldUseDefaults() throws Exception {
        when(bookingService.getUserBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(bookingService).getUserBookings(1L, "ALL", 0, 10);
    }

    @Test
    void createBooking_WithoutUserIdHeader_ShouldReturnInternalServerError() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void approveBooking_WithoutApprovedParam_ShouldReturnInternalServerError() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isInternalServerError());
    }
}
