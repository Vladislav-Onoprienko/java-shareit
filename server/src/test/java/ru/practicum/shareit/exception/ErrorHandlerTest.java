package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Test
    void handleNotFound_ShouldReturnNotFoundStatus() {
        NotFoundException exception = new NotFoundException("User not found");

        ErrorResponse response = errorHandler.handleNotFound(exception);

        assertNotNull(response);
        assertEquals("User not found", response.getError());
    }

    @Test
    void handleConflict_ShouldReturnConflictStatus() {
        ConflictException exception = new ConflictException("Email already exists");

        ErrorResponse response = errorHandler.handleConflict(exception);

        assertNotNull(response);
        assertEquals("Email already exists", response.getError());
    }

    @Test
    void handleValidation_ShouldReturnBadRequestStatus() {
        ValidationException exception = new ValidationException("Invalid email format");

        ErrorResponse response = errorHandler.handleValidation(exception);

        assertNotNull(response);
        assertEquals("Validation error: Invalid email format", response.getError());
    }

    @Test
    void handleUnavailableItem_ShouldReturnBadRequestStatus() {
        UnavailableItemException exception = new UnavailableItemException("Item is not available");

        ErrorResponse response = errorHandler.handleUnavailableItem(exception);

        assertNotNull(response);
        assertEquals("Item is not available", response.getError());
    }

    @Test
    void handleForbidden_ShouldReturnForbiddenStatus() {
        ForbiddenException exception = new ForbiddenException("Access denied");

        ErrorResponse response = errorHandler.handleForbidden(exception);

        assertNotNull(response);
        assertEquals("Access denied", response.getError());
    }

    @Test
    void handleInternalError_ShouldReturnInternalServerErrorStatus() {
        Exception exception = new Exception("Unexpected error");

        ErrorResponse response = errorHandler.handleInternalError(exception);

        assertNotNull(response);
        assertEquals("Internal server error", response.getError());
    }

    @Test
    void errorResponse_ShouldHaveCorrectStructure() {
        String errorMessage = "Test error message";

        ErrorResponse response = new ErrorResponse(errorMessage);

        assertNotNull(response);
        assertEquals(errorMessage, response.getError());

        ErrorResponse sameResponse = new ErrorResponse(errorMessage);
        ErrorResponse differentResponse = new ErrorResponse("Different message");

        assertEquals(response, sameResponse);
        assertNotEquals(response, differentResponse);
        assertEquals(response.hashCode(), sameResponse.hashCode());
        assertTrue(response.toString().contains(errorMessage));
    }

    @Test
    void exceptionClasses_ShouldHaveCorrectConstructors() {
        NotFoundException notFound = new NotFoundException("Not found");
        assertEquals("Not found", notFound.getMessage());

        ConflictException conflict = new ConflictException("Conflict");
        assertEquals("Conflict", conflict.getMessage());

        ValidationException validation = new ValidationException("Validation");
        assertEquals("Validation", validation.getMessage());

        UnavailableItemException unavailable = new UnavailableItemException("Unavailable");
        assertEquals("Unavailable", unavailable.getMessage());

        ForbiddenException forbidden = new ForbiddenException("Forbidden");
        assertEquals("Forbidden", forbidden.getMessage());

        ItemNotFoundException itemNotFound = new ItemNotFoundException("Item not found");
        assertEquals("Item not found", itemNotFound.getMessage());
    }
}
