package com.athletics.apigateway.presentationlayer.sponsor;

import com.athletics.apigateway.businesslayer.sponsor.SponsorService;
import com.athletics.apigateway.domainclientlayer.sponsor.SponsorLevelEnum;
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
class SponsorControllerUnitTest {

    @Autowired
    private SponsorController sponsorController;

    @MockitoBean
    private SponsorService sponsorService;

    private final String VALID_ID        = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
    private final String INVALID_ID      = "bad-id";
    private final String NOT_FOUND_ID    = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";

    @Test
    void whenNoSponsorsExist_thenReturnEmptyList() {
        when(sponsorService.getAllSponsors())
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<SponsorResponseModel>> resp =
                sponsorController.getAllSponsors();

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(sponsorService).getAllSponsors();
    }

    @Test
    void whenGetByIdValid_thenReturnSponsor() {
        SponsorResponseModel mock = new SponsorResponseModel(
                VALID_ID, "Nike", SponsorLevelEnum.PLATINUM, 200000.0
        );
        when(sponsorService.getSponsorById(VALID_ID))
                .thenReturn(mock);

        ResponseEntity<SponsorResponseModel> resp =
                sponsorController.getSponsorById(VALID_ID);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("Nike", resp.getBody().getSponsorName());
        verify(sponsorService).getSponsorById(VALID_ID);
    }

    @Test
    void whenGetByIdInvalid_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid sponsorId provided: " + INVALID_ID))
                .when(sponsorService).getSponsorById(INVALID_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> sponsorController.getSponsorById(INVALID_ID)
        );
        assertEquals("Invalid sponsorId provided: " + INVALID_ID, ex.getMessage());
        verify(sponsorService).getSponsorById(INVALID_ID);
    }

    @Test
    void whenGetByIdNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Sponsor not found with ID: " + NOT_FOUND_ID))
                .when(sponsorService).getSponsorById(NOT_FOUND_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> sponsorController.getSponsorById(NOT_FOUND_ID)
        );
        assertEquals("Sponsor not found with ID: " + NOT_FOUND_ID, ex.getMessage());
        verify(sponsorService).getSponsorById(NOT_FOUND_ID);
    }

    @Test
    void whenCreateValid_thenReturnCreated() {
        SponsorRequestModel req = new SponsorRequestModel(
                null, "NewSponsor", SponsorLevelEnum.GOLD, 75000.0
        );
        SponsorResponseModel created = new SponsorResponseModel(
                "ccccccc1-1ccc-1ccc-1ccc-ccccccccccc1",
                "NewSponsor",
                SponsorLevelEnum.GOLD,
                75000.0
        );
        when(sponsorService.createSponsor(req))
                .thenReturn(created);

        ResponseEntity<SponsorResponseModel> resp =
                sponsorController.createSponsor(req);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals("NewSponsor", resp.getBody().getSponsorName());
        verify(sponsorService).createSponsor(req);
    }

    @Test
    void whenCreateInvalid_thenThrowInvalidInput() {
        SponsorRequestModel req = new SponsorRequestModel(
                INVALID_ID, "", SponsorLevelEnum.BRONZE, -100.0
        );
        doThrow(new InvalidInputException("Invalid sponsor data"))
                .when(sponsorService).createSponsor(req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> sponsorController.createSponsor(req)
        );
        assertEquals("Invalid sponsor data", ex.getMessage());
        verify(sponsorService).createSponsor(req);
    }

    @Test
    void whenUpdateValid_thenReturnOk() {
        SponsorRequestModel req = new SponsorRequestModel(
                VALID_ID, "NikeUpdated", SponsorLevelEnum.PLATINUM, 250000.0
        );
        SponsorResponseModel updated = new SponsorResponseModel(
                VALID_ID, "NikeUpdated", SponsorLevelEnum.PLATINUM, 250000.0
        );
        when(sponsorService.updateSponsor(VALID_ID, req))
                .thenReturn(updated);

        ResponseEntity<SponsorResponseModel> resp =
                sponsorController.updateSponsor(VALID_ID, req);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertEquals("NikeUpdated", resp.getBody().getSponsorName());
        verify(sponsorService).updateSponsor(VALID_ID, req);
    }

    @Test
    void whenUpdateInvalid_thenThrowInvalidInput() {
        SponsorRequestModel req = new SponsorRequestModel(
                INVALID_ID, "X", SponsorLevelEnum.GOLD, 5000.0
        );
        doThrow(new InvalidInputException("Invalid sponsorId provided: " + INVALID_ID))
                .when(sponsorService).updateSponsor(INVALID_ID, req);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> sponsorController.updateSponsor(INVALID_ID, req)
        );
        assertEquals("Invalid sponsorId provided: " + INVALID_ID, ex.getMessage());
        verify(sponsorService).updateSponsor(INVALID_ID, req);
    }

    @Test
    void whenUpdateNotFound_thenThrowNotFound() {
        SponsorRequestModel req = new SponsorRequestModel(
                NOT_FOUND_ID, "Ghost", SponsorLevelEnum.SILVER, 1000.0
        );
        doThrow(new NotFoundException("Sponsor not found with ID: " + NOT_FOUND_ID))
                .when(sponsorService).updateSponsor(NOT_FOUND_ID, req);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> sponsorController.updateSponsor(NOT_FOUND_ID, req)
        );
        assertEquals("Sponsor not found with ID: " + NOT_FOUND_ID, ex.getMessage());
        verify(sponsorService).updateSponsor(NOT_FOUND_ID, req);
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() {
        doNothing().when(sponsorService)
                .deleteSponsor(VALID_ID);

        ResponseEntity<Void> resp =
                sponsorController.deleteSponsor(VALID_ID);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(sponsorService).deleteSponsor(VALID_ID);
    }

    @Test
    void whenDeleteInvalid_thenThrowInvalidInput() {
        doThrow(new InvalidInputException("Invalid sponsorId provided: " + INVALID_ID))
                .when(sponsorService).deleteSponsor(INVALID_ID);

        InvalidInputException ex = assertThrows(
                InvalidInputException.class,
                () -> sponsorController.deleteSponsor(INVALID_ID)
        );
        assertEquals("Invalid sponsorId provided: " + INVALID_ID, ex.getMessage());
        verify(sponsorService).deleteSponsor(INVALID_ID);
    }

    @Test
    void whenDeleteNotFound_thenThrowNotFound() {
        doThrow(new NotFoundException("Sponsor not found with ID: " + NOT_FOUND_ID))
                .when(sponsorService).deleteSponsor(NOT_FOUND_ID);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> sponsorController.deleteSponsor(NOT_FOUND_ID)
        );
        assertEquals("Sponsor not found with ID: " + NOT_FOUND_ID, ex.getMessage());
        verify(sponsorService).deleteSponsor(NOT_FOUND_ID);
    }
}
