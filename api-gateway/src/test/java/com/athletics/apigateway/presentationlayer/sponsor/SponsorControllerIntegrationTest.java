package com.athletics.apigateway.presentationlayer.sponsor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.athletics.apigateway.domainclientlayer.sponsor.SponsorLevelEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SponsorControllerIntegrationTest {

    @Autowired WebTestClient webClient;
    @Autowired RestTemplate restTemplate;
    @Autowired ObjectMapper mapper;

    private MockRestServiceServer mockServer;

    private static final String BASE_URI         = "/api/v1/sponsors";
    private static final String SERVICE_BASE_URI = "http://localhost:7002/api/v1/sponsors";

    private static final String VALID_ID        = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
    private static final String NOT_FOUND_ID    = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private static final String INVALID_ID      = "bad-id";

    @BeforeEach
    void init() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    void whenGetAllSponsors_thenReturnList() throws Exception {
        SponsorResponseModel s1 = new SponsorResponseModel(VALID_ID, "Nike", SponsorLevelEnum.PLATINUM, 200000.0);
        SponsorResponseModel s2 = new SponsorResponseModel("aaaaaaa2-2aaa-2aaa-2aaa-aaaaaaaaaaa2", "Adidas", SponsorLevelEnum.GOLD, 150000.0);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(new SponsorResponseModel[]{s1, s2}),
                        MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(SponsorResponseModel.class)
                .value(list -> assertEquals(2, list.size()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdValid_thenReturnSponsor() throws Exception {
        SponsorResponseModel s = new SponsorResponseModel(VALID_ID, "Nike", SponsorLevelEnum.PLATINUM, 200000.0);
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(s), MediaType.APPLICATION_JSON));

        webClient.get().uri(BASE_URI + "/" + VALID_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SponsorResponseModel.class)
                .value(resp -> assertEquals("Nike", resp.getSponsorName()));

        mockServer.verify();
    }

    @Test
    void whenGetByIdInvalid_thenReturnUnprocessableEntity() throws Exception {
        mockServer.expect(once(),
                        requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid sponsorId provided: " + INVALID_ID + "\"}"));

        webClient.get().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenGetByIdNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get().uri(BASE_URI + "/" + NOT_FOUND_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenCreateValid_thenReturnsCreated() throws Exception {
        SponsorRequestModel req = new SponsorRequestModel(VALID_ID, "TestSponsor", SponsorLevelEnum.SILVER, 5000.0);
        SponsorResponseModel created = new SponsorResponseModel(VALID_ID, "TestSponsor", SponsorLevelEnum.SILVER, 5000.0);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(mapper.writeValueAsString(req)))
                .andRespond(withStatus(HttpStatus.CREATED)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(mapper.writeValueAsString(created)));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SponsorResponseModel.class)
                .value(resp -> assertEquals("TestSponsor", resp.getSponsorName()));

        mockServer.verify();
    }

    @Test
    void whenCreateBlankName_thenReturnUnprocessableEntity() throws Exception {
        SponsorRequestModel req = new SponsorRequestModel(VALID_ID, "  ", SponsorLevelEnum.BRONZE, 2000.0);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Sponsor name cannot be empty.\"}"));

        webClient.post().uri(BASE_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Sponsor name cannot be empty.");

        mockServer.verify();
    }

    @Test
    void whenUpdateValid_thenReturnsOk() throws Exception {
        SponsorRequestModel update = new SponsorRequestModel(VALID_ID, "NikeUpdated", SponsorLevelEnum.GOLD, 300000.0);
        SponsorResponseModel updated = new SponsorResponseModel(VALID_ID, "NikeUpdated", SponsorLevelEnum.GOLD, 300000.0);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withNoContent());

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(mapper.writeValueAsString(updated), MediaType.APPLICATION_JSON));

        webClient.put().uri(BASE_URI + "/" + VALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SponsorResponseModel.class)
                .value(resp -> assertEquals("NikeUpdated", resp.getSponsorName()));

        mockServer.verify();
    }

    @Test
    void whenUpdateInvalid_thenReturnUnprocessableEntity() throws Exception {
        SponsorRequestModel any = new SponsorRequestModel(
                INVALID_ID, "X", SponsorLevelEnum.SILVER, 5000.0);

        // stub downstream to return 422 for invalid ID
        mockServer.expect(once(),
                        requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid sponsorId provided: " + INVALID_ID + "\"}"));

        webClient.put().uri(BASE_URI + "/" + INVALID_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(any)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);

        mockServer.verify();
    }

    @Test
    void whenUpdateNotFound_thenReturnNotFound() throws Exception {
        SponsorRequestModel any = new SponsorRequestModel(NOT_FOUND_ID, "Ghost", SponsorLevelEnum.GOLD, 5000.0);

        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + NOT_FOUND_ID))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.put().uri(BASE_URI + "/" + NOT_FOUND_ID)
                .bodyValue(any)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenDeleteValid_thenReturnNoContent() throws Exception {
        mockServer.expect(once(), requestTo(SERVICE_BASE_URI + "/" + VALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withNoContent());

        webClient.delete().uri(BASE_URI + "/" + VALID_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void whenDeleteInvalid_thenReturnUnprocessableEntity() throws Exception {
        mockServer.expect(once(),
                        requestTo(SERVICE_BASE_URI + "/" + INVALID_ID))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"message\":\"Invalid sponsorId provided: " + INVALID_ID + "\"}"));

        webClient.delete().uri(BASE_URI + "/" + INVALID_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid sponsorId provided: " + INVALID_ID);

        mockServer.verify();
    }
}
