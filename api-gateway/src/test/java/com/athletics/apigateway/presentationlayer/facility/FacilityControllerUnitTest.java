package com.athletics.apigateway.presentationlayer.facility;

import com.athletics.apigateway.businesslayer.facility.FacilityService;
import com.athletics.apigateway.utils.exceptions.InvalidInputException;
import com.athletics.apigateway.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class FacilityControllerUnitTest {

    @Autowired
    private FacilityController facilityController;

    @MockitoBean
    private FacilityService facilityService;

    private final String VALID_ID        = "fac11111-1111-1111-1111-111111111111";
    private final String INVALID_ID      = "bad-fac-id";
    private final String NOT_FOUND_ID    = "fac99999-9999-9999-9999-999999999999";

    @Test
    void whenNoFacilitiesExist_thenReturnEmptyList() {
        when(facilityService.getAllFacilities())
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<FacilityResponseModel>> resp =
                facilityController.getAllFacilities();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(facilityService).getAllFacilities();
    }

    @Test
    void whenGetByIdValid_thenReturnFacility() {
        FacilityResponseModel mockFac = new FacilityResponseModel(
                VALID_ID, "Stadium1", 50000, "City1"
        );
        when(facilityService.getFacilityById(VALID_ID))
                .thenReturn(mockFac);

        ResponseEntity<FacilityResponseModel> resp =
                facilityController.getFacilityById(VALID_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Stadium1", resp.getBody().getFacilityName());
        verify(facilityService).getFacilityById(VALID_ID);
    }

    @Test
    void whenGetByIdInvalid_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid facilityId provided: " + INVALID_ID))
                .when(facilityService).getFacilityById(INVALID_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> facilityController.getFacilityById(INVALID_ID)
        );
        assertEquals("Invalid facilityId provided: " + INVALID_ID, ex.getMessage());
        verify(facilityService).getFacilityById(INVALID_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Facility not found with ID: " + NOT_FOUND_ID))
                .when(facilityService).getFacilityById(NOT_FOUND_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> facilityController.getFacilityById(NOT_FOUND_ID)
        );
        assertEquals("Facility not found with ID: " + NOT_FOUND_ID, ex.getMessage());
        verify(facilityService).getFacilityById(NOT_FOUND_ID);
    }

    @Test
    void whenCreateValid_thenReturnCreated() {
        FacilityRequestModel req = new FacilityRequestModel(
                null, "NewArena", 30000, "NewCity"
        );
        FacilityResponseModel created = new FacilityResponseModel(
                "fac55555-5555-5555-5555-555555555555",
                "NewArena", 30000, "NewCity"
        );
        when(facilityService.createFacility(req))
                .thenReturn(created);

        ResponseEntity<FacilityResponseModel> resp =
                facilityController.createFacility(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("NewArena", resp.getBody().getFacilityName());
        verify(facilityService).createFacility(req);
    }

    @Test
    void whenCreateInvalid_thenThrowInvalidInput() {
        FacilityRequestModel req = new FacilityRequestModel(
                INVALID_ID, "", 0, ""
        );
        doThrow(new InvalidInputException("Invalid facility data"))
                .when(facilityService).createFacility(req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> facilityController.createFacility(req)
        );
        assertEquals("Invalid facility data", ex.getMessage());
        verify(facilityService).createFacility(req);
    }

    @Test
    void whenUpdateValid_thenReturnOk() {
        FacilityRequestModel req = new FacilityRequestModel(
                VALID_ID, "UpdatedArena", 60000, "UpdatedCity"
        );
        FacilityResponseModel updated = new FacilityResponseModel(
                VALID_ID, "UpdatedArena", 60000, "UpdatedCity"
        );
        when(facilityService.updateFacility(VALID_ID, req))
                .thenReturn(updated);

        ResponseEntity<FacilityResponseModel> resp =
                facilityController.updateFacility(VALID_ID, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("UpdatedArena", resp.getBody().getFacilityName());
        verify(facilityService).updateFacility(VALID_ID, req);
    }

    @Test
    void whenUpdateInvalid_thenThrowInvalidInput() {
        FacilityRequestModel req = new FacilityRequestModel(
                INVALID_ID, "X", 100, "Y"
        );
        doThrow(new InvalidInputException("Invalid facilityId provided: " + INVALID_ID))
                .when(facilityService).updateFacility(INVALID_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> facilityController.updateFacility(INVALID_ID, req)
        );
        assertEquals("Invalid facilityId provided: " + INVALID_ID, ex.getMessage());
        verify(facilityService).updateFacility(INVALID_ID, req);
    }

    @Test
    void whenUpdateNotFound_thenThrowNotFound() {
        FacilityRequestModel req = new FacilityRequestModel(
                NOT_FOUND_ID, "X", 100, "Y"
        );
        doThrow(new NotFoundException("Facility not found with ID: " + NOT_FOUND_ID))
                .when(facilityService).updateFacility(NOT_FOUND_ID, req);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> facilityController.updateFacility(NOT_FOUND_ID, req)
        );
        assertEquals("Facility not found with ID: " + NOT_FOUND_ID, ex.getMessage());
        verify(facilityService).updateFacility(NOT_FOUND_ID, req);
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() {
        doNothing().when(facilityService)
                .deleteFacility(VALID_ID);

        ResponseEntity<Void> resp =
                facilityController.deleteFacility(VALID_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(facilityService).deleteFacility(VALID_ID);
    }

    @Test
    void whenDeleteInvalid_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid facilityId provided: " + INVALID_ID))
                .when(facilityService).deleteFacility(INVALID_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> facilityController.deleteFacility(INVALID_ID)
        );
        assertEquals("Invalid facilityId provided: " + INVALID_ID, ex.getMessage());
        verify(facilityService).deleteFacility(INVALID_ID);
    }

    @Test
    void whenDeleteNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Facility not found with ID: " + NOT_FOUND_ID))
                .when(facilityService).deleteFacility(NOT_FOUND_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> facilityController.deleteFacility(NOT_FOUND_ID)
        );
        assertEquals("Facility not found with ID: " + NOT_FOUND_ID, ex.getMessage());
        verify(facilityService).deleteFacility(NOT_FOUND_ID);
    }
}
