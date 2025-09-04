package ru.practicum.shareit.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class ErrorResponse {
    private String error;
}
