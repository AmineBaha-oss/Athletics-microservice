package com.athletics.sponsor.presentationlayer;

import com.athletics.sponsor.dataaccesslayer.Sponsor;
import com.athletics.sponsor.dataaccesslayer.SponsorIdentifier;
import com.athletics.sponsor.dataaccesslayer.SponsorRepository;
import com.athletics.sponsor.dataaccesslayer.SponsorLevelEnum;
import com.athletics.sponsor.utils.exceptions.InsufficientSponsorAmountException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Sql({"/data-h2.sql"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SponsorControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private SponsorRepository sponsorRepository;

    private final String BASE_URI = "/api/v1/sponsors";

    private final String VALID_ID         = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
    private final String ANOTHER_VALID_ID = "aaaaaaa2-2aaa-2aaa-2aaa-aaaaaaaaaaa2";
    private final String NON_EXISTENT_ID  = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private final String INVALID_ID       = "bad-id";
    private final String NEW_ID           = "ccccccc3-3ccc-3ccc-3ccc-cccccccccccc";

    @Test
    void whenGetAllSponsors_thenReturnList() {
        long expected = sponsorRepository.count();

        webClient.get()
                .uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(SponsorResponseModel.class)
                .value(list -> assertEquals(expected, list.size()));
    }

    @Test
    void whenGetByIdValid_thenReturnSponsor() {
        webClient.get()
                .uri(BASE_URI + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SponsorResponseModel.class)
                .value(s -> {
                    assertEquals(VALID_ID, s.getSponsorId());
                    assertEquals("Nike", s.getSponsorName());
                    assertEquals(SponsorLevelEnum.PLATINUM, s.getSponsorLevel());
                    assertEquals(200000.00, s.getSponsorAmount());
                });
    }

    @Test
    void whenGetByIdInvalid_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);
    }

    @Test
    void whenGetByIdNotFound_thenReturnNotFound() {
        webClient.get()
                .uri(BASE_URI + "/" + NON_EXISTENT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Sponsor not found with ID: " + NON_EXISTENT_ID);
    }

    @Test
    void whenCreateValid_thenReturnsCreated() {
        SponsorRequestModel req = new SponsorRequestModel(
                NEW_ID,
                "TestSponsor",
                SponsorLevelEnum.GOLD,
                5000.00
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SponsorResponseModel.class)
                .value(s -> {
                    assertEquals(NEW_ID, s.getSponsorId());
                    assertEquals(req.getSponsorName(), s.getSponsorName());
                    assertEquals(req.getSponsorLevel(), s.getSponsorLevel());
                    assertEquals(req.getSponsorAmount(), s.getSponsorAmount());
                });
    }

    @Test
    void whenCreateBlankName_thenReturnUnprocessableEntity() {
        SponsorRequestModel req = new SponsorRequestModel(
                NEW_ID,
                "  ",
                SponsorLevelEnum.SILVER,
                2000.00
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Sponsor name cannot be empty.");
    }

    @Test
    void whenCreateInsufficientAmount_thenReturnUnprocessableEntity() {
        SponsorRequestModel req = new SponsorRequestModel(
                NEW_ID,
                "SmallSponsor",
                SponsorLevelEnum.BRONZE,
                500.00
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The sponsor amount is too low. Minimum required amount is 1000.0");
    }

    @Test
    void whenCreateDuplicate_thenReturnUnprocessableEntity() {
        SponsorRequestModel dup = new SponsorRequestModel(
                VALID_ID,
                "NikeDup",
                SponsorLevelEnum.PLATINUM,
                200000.00
        );

        webClient.post()
                .uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dup)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("A sponsor with the ID " + VALID_ID + " already exists, causing an identity clash.");
    }

    @Test
    void whenUpdateValid_thenReturnsCreated() {
        SponsorRequestModel update = new SponsorRequestModel(
                VALID_ID,
                "NikeUpdated",
                SponsorLevelEnum.GOLD,
                300000.00
        );

        webClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SponsorResponseModel.class)
                .value(s -> {
                    assertEquals(VALID_ID, s.getSponsorId());
                    assertEquals("NikeUpdated", s.getSponsorName());
                    assertEquals(SponsorLevelEnum.GOLD, s.getSponsorLevel());
                    assertEquals(300000.00, s.getSponsorAmount());
                });
    }

    @Test
    void whenUpdateInvalid_thenReturnUnprocessableEntity() {
        SponsorRequestModel req = new SponsorRequestModel(
                INVALID_ID,
                "X",
                SponsorLevelEnum.SILVER,
                5000.00
        );

        webClient.put()
                .uri(BASE_URI + "/" + INVALID_ID)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);
    }

    @Test
    void whenUpdateNotFound_thenReturnNotFound() {
        SponsorRequestModel req = new SponsorRequestModel(
                NON_EXISTENT_ID,
                "Ghost",
                SponsorLevelEnum.GOLD,
                5000.00
        );

        webClient.put()
                .uri(BASE_URI + "/" + NON_EXISTENT_ID)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Sponsor not found with ID: " + NON_EXISTENT_ID);
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() {
        webClient.delete()
                .uri(BASE_URI + "/" + ANOTHER_VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        webClient.get()
                .uri(BASE_URI + "/" + ANOTHER_VALID_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void whenDeleteInvalid_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);
    }

    @Test
    void whenDeleteNotFound_thenReturnNotFound() {
        webClient.delete()
                .uri(BASE_URI + "/" + NON_EXISTENT_ID)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Sponsor not found with ID: " + NON_EXISTENT_ID);
    }

    @Test
    void whenPatchValid_thenSponsorLevelUpdated() {
        Sponsor before = sponsorRepository.findBySponsorIdentifier_SponsorId(ANOTHER_VALID_ID);
        assertEquals(SponsorLevelEnum.GOLD, before.getSponsorLevel());

        webClient.patch()
                .uri(BASE_URI + "/" + ANOTHER_VALID_ID + "/level")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("silver")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaTypes.HAL_JSON)
                .expectBody(SponsorResponseModel.class)
                .value(s -> {
                    assertEquals(ANOTHER_VALID_ID, s.getSponsorId());
                    assertEquals(SponsorLevelEnum.SILVER, s.getSponsorLevel());
                });

        Sponsor after = sponsorRepository.findBySponsorIdentifier_SponsorId(ANOTHER_VALID_ID);
        assertEquals(SponsorLevelEnum.SILVER, after.getSponsorLevel());
    }

    @Test
    void whenPatchInvalidId_thenReturnUnprocessableEntity() {
        webClient.patch()
                .uri(BASE_URI + "/" + INVALID_ID + "/level")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("bronze")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);
    }

    @Test
    void whenPatchNotFound_thenReturnNotFound() {
        webClient.patch()
                .uri(BASE_URI + "/" + NON_EXISTENT_ID + "/level")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("platinum")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Sponsor not found with ID: " + NON_EXISTENT_ID);
    }

    @Test
    void whenPatchInvalidLevel_thenReturnUnprocessableEntity() {
        webClient.patch()
                .uri(BASE_URI + "/" + VALID_ID + "/level")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("ultra")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsor level: ultra");
    }
    @Test
    void whenUpdateInsufficientAmount_thenReturnUnprocessableEntity() {
        SponsorRequestModel req = new SponsorRequestModel(
                VALID_ID,
                "Nike",
                SponsorLevelEnum.BRONZE,
                500.00
        );

        webClient.put()
                .uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The sponsor amount is too low. Minimum required amount is 1000.0");
    }


}