package com.athletics.competition.presentationlayer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import com.athletics.competition.dataaccesslayer.Competition;
import com.athletics.competition.dataaccesslayer.CompetitionRepository;
import com.athletics.competition.domainclientLayer.facility.FacilityModel;
import com.athletics.competition.domainclientLayer.facility.FacilityServiceClient;
import com.athletics.competition.domainclientLayer.sponsor.SponsorModel;
import com.athletics.competition.domainclientLayer.sponsor.SponsorServiceClient;
import com.athletics.competition.domainclientLayer.team.TeamModel;
import com.athletics.competition.domainclientLayer.team.TeamServiceClient;
import com.athletics.competition.presentationlayer.CompetitionRequestModel;
import com.athletics.competition.presentationlayer.CompetitionResponseModel;
import com.athletics.competition.dataaccesslayer.CompetitionStatusEnum;
import com.athletics.competition.dataaccesslayer.CompetitionResultEnum;
import com.athletics.competition.utils.CompetitionHttpErrorInfo;
import com.athletics.competition.utils.exceptions.InvalidInputException;
import com.athletics.competition.utils.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CompetitionControllerIntegrationTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CompetitionRepository competitionRepository;

    private MockRestServiceServer mockServer;
    private ObjectMapper mapper;

    private static final String BASE_URI                  = "/api/v1/teams";
    private static final String TEAM_SERVICE_BASE_URI     = "http://localhost:7001/api/v1/teams";
    private static final String SPONSOR_SERVICE_BASE_URI  = "http://localhost:7002/api/v1/sponsors";
    private static final String FACILITY_SERVICE_BASE_URI = "http://localhost:7003/api/v1/facilities";

    private static final String VALID_TEAM_ID             = "11111111-1111-1111-1111-111111111111";
    private static String       VALID_COMPETITION_ID;
    private static final String NOT_FOUND_TEAM_ID         = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String NOT_FOUND_COMPETITION_ID  = "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb";
    private static final String INVALID_UUID              = "bad-uuid";

    @BeforeEach
    void init() {
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        for (HttpMessageConverter<?> conv : restTemplate.getMessageConverters()) {
            if (conv instanceof MappingJackson2HttpMessageConverter mj) {
                mj.setObjectMapper(mapper);
            }
        }

        mockServer = MockRestServiceServer.createServer(restTemplate);

        assertTrue(competitionRepository.count() > 0);

        List<Competition> comps = competitionRepository.findAllByTeam_TeamId(VALID_TEAM_ID);
        assertFalse(comps.isEmpty());
        VALID_COMPETITION_ID = comps.get(0).getCompetitionIdentifier().getCompetitionId();
    }


    @Test
    void whenGetAllCompetitionsExists_thenReturnListOfCompetitions() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CompetitionResponseModel.class)
                .value(list -> {
                    assertNotNull(list);
                    long expected = competitionRepository.findAllByTeam_TeamId(VALID_TEAM_ID).size();
                    assertEquals(expected, list.size());
                });

        mockServer.verify();
    }

    @Test
    void whenGetAllCompetitionsWithInvalidTeamId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_UUID);
    }

    @Test
    void whenGetAllCompetitionsTeamNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + NOT_FOUND_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        webClient.get()
                .uri(BASE_URI + "/" + NOT_FOUND_TEAM_ID + "/competitions")
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }


    @Test
    void whenGetCompetitionByIdExists_thenReturnCompetition() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompetitionResponseModel.class)
                .value(resp -> assertEquals(VALID_COMPETITION_ID, resp.getCompetitionId()));

        mockServer.verify();
    }

    @Test
    void whenGetCompetitionByIdWithInvalidTeamId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions/" + VALID_COMPETITION_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid ID provided");
    }

    @Test
    void whenGetCompetitionByIdWithInvalidCompetitionId_thenReturnUnprocessableEntity() {
        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + INVALID_UUID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid ID provided");
    }

    @Test
    void whenGetCompetitionByIdNotFound_thenReturnNotFound() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        webClient.get()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }


    @Test
    void whenCreateCompetitionWithValidData_thenReturnCreatedCompetition() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        String sponsorId = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
        SponsorModel sponsorModel = SponsorModel.builder()
                .sponsorId(sponsorId)
                .sponsorName("Nike")
                .sponsorLevel("GOLD")
                .sponsorAmount(100000.00)
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(sponsorModel),
                        MediaType.APPLICATION_JSON
                ));

        String facilityId = "fac11111-1111-1111-1111-111111111111";
        FacilityModel facilityModel = FacilityModel.builder()
                .facilityId(facilityId)
                .facilityName("Olympic Stadium")
                .capacity(70000)
                .location("Montreal, QC")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(FACILITY_SERVICE_BASE_URI + "/" + facilityId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(facilityModel),
                        MediaType.APPLICATION_JSON
                ));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Test Championship")
                .competitionDate(LocalDate.now().plusDays(1))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(sponsorId)
                .facilityId(facilityId)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CompetitionResponseModel.class)
                .value(resp -> {
                    assertNotNull(resp.getCompetitionId());
                    assertEquals("Test Championship", resp.getCompetitionName());
                });

        mockServer.verify();
    }

    @Test
    void whenCreateCompetitionWithInvalidTeamId_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Invalid Team")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Invalid teamId provided: " + INVALID_UUID);
    }

    @Test
    void whenCreateCompetitionTeamNotFound_thenReturnNotFound() throws Exception {
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + NOT_FOUND_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("No Team")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + NOT_FOUND_TEAM_ID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenCreateCompetitionSponsorNotFound_thenReturnNotFound() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        String badSponsorId = "00000000-0000-0000-0000-000000000000";
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + badSponsorId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Bad Sponsor")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(badSponsorId)
                .facilityId(null)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenCreateCompetitionFacilityNotFound_thenReturnNotFound() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        String sponsorId = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
        SponsorModel sponsorModel = SponsorModel.builder()
                .sponsorId(sponsorId)
                .sponsorName("Nike")
                .sponsorLevel("GOLD")
                .sponsorAmount(100000.00)
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(sponsorModel),
                        MediaType.APPLICATION_JSON
                ));

        String badFacilityId = "fac00000-0000-0000-0000-000000000000";
        mockServer.expect(once(),
                        requestTo(new URI(FACILITY_SERVICE_BASE_URI + "/" + badFacilityId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Bad Facility")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(sponsorId)
                .facilityId(badFacilityId)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenCreateCompetitionDateTooFar_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Too Far")
                .competitionDate(LocalDate.now().plusYears(2))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.post()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The competition date must be within one year of today.");
    }


    @Test
    void whenUpdateCompetitionWithValidData_thenReturnUpdatedCompetition() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        String sponsorId = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
        SponsorModel sponsorModel = SponsorModel.builder()
                .sponsorId(sponsorId)
                .sponsorName("Nike")
                .sponsorLevel("GOLD")
                .sponsorAmount(100000.00)
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(sponsorModel),
                        MediaType.APPLICATION_JSON
                ));

        String facilityId = "fac11111-1111-1111-1111-111111111111";
        FacilityModel facilityModel = FacilityModel.builder()
                .facilityId(facilityId)
                .facilityName("Olympic Stadium")
                .capacity(70000)
                .location("Montreal, QC")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(FACILITY_SERVICE_BASE_URI + "/" + facilityId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(facilityModel),
                        MediaType.APPLICATION_JSON
                ));
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Updated Name")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.ONGOING)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(sponsorId)
                .facilityId(facilityId)
                .build();
        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompetitionResponseModel.class)
                .value(resp -> {
                    assertEquals(VALID_COMPETITION_ID, resp.getCompetitionId());
                    assertEquals("Updated Name", resp.getCompetitionName());
                });
        mockServer.verify();
    }
    @Test
    void whenUpdateCompetitionWithInvalidTeamId_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Test")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions/" + VALID_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid ID provided");
    }

    @Test
    void whenUpdateCompetitionWithInvalidCompetitionId_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Test")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + INVALID_UUID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid ID provided");
    }

    @Test
    void whenUpdateCompetitionNotFound_thenReturnNotFound() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Ghost")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenUpdateCompetitionSponsorNotFound_thenReturnNotFound() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        String badSponsorId = "00000000-0000-0000-0000-000000000000";
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + badSponsorId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Bad Sponsor")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(badSponsorId)
                .facilityId(null)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenUpdateCompetitionFacilityNotFound_thenReturnNotFound() throws Exception {
        TeamModel teamModel = TeamModel.builder()
                .teamId(VALID_TEAM_ID)
                .teamName("Montreal Eagles")
                .coachName("John Smith")
                .teamLevel("COLLEGE")
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI + "/" + VALID_TEAM_ID)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(teamModel),
                        MediaType.APPLICATION_JSON
                ));

        String sponsorId = "aaaaaaa1-1aaa-1aaa-1aaa-aaaaaaaaaaa1";
        SponsorModel sponsorModel = SponsorModel.builder()
                .sponsorId(sponsorId)
                .sponsorName("Nike")
                .sponsorLevel("GOLD")
                .sponsorAmount(100000.00)
                .build();
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(sponsorModel),
                        MediaType.APPLICATION_JSON
                ));

        String badFacilityId = "fac00000-0000-0000-0000-000000000000";
        mockServer.expect(once(),
                        requestTo(new URI(FACILITY_SERVICE_BASE_URI + "/" + badFacilityId)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Bad Facility")
                .competitionDate(LocalDate.now())
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.DRAW)
                .sponsorId(sponsorId)
                .facilityId(badFacilityId)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isNotFound();

        mockServer.verify();
    }

    @Test
    void whenUpdateCompetitionDateTooFar_thenReturnUnprocessableEntity() {
        CompetitionRequestModel req = CompetitionRequestModel.builder()
                .competitionName("Too Far")
                .competitionDate(LocalDate.now().plusYears(2))
                .competitionStatus(CompetitionStatusEnum.SCHEDULED)
                .competitionResult(CompetitionResultEnum.LOSS)
                .sponsorId(null)
                .facilityId(null)
                .build();

        webClient.put()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(req)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("The competition date must be within one year of today.");
    }


    @Test
    void whenDeleteCompetitionWithValidData_thenReturnNoContent() throws Exception {
        String sponsorId = competitionRepository
                .findByTeam_TeamIdAndCompetitionIdentifier_CompetitionId(VALID_TEAM_ID, VALID_COMPETITION_ID)
                .getSponsor().getSponsorId();

        SponsorModel noneSponsor = SponsorModel.builder()
                .sponsorId(sponsorId)
                .sponsorName("Nike")
                .sponsorLevel("NONE")
                .sponsorAmount(0.0)
                .build();

        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId + "/level")))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess(
                        mapper.writeValueAsString(noneSponsor),
                        MediaType.APPLICATION_JSON
                ));

        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + VALID_COMPETITION_ID)
                .exchange()
                .expectStatus().isNoContent();

        mockServer.verify();
    }

    @Test
    void whenDeleteCompetitionWithInvalidTeamId_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + INVALID_UUID + "/competitions/" + VALID_COMPETITION_ID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid ID provided");
    }

    @Test
    void whenDeleteCompetitionWithInvalidCompetitionId_thenReturnUnprocessableEntity() {
        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + INVALID_UUID)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Invalid ID provided");
    }

    @Test
    void whenDeleteCompetitionNotFound_thenReturnNotFound() {
        webClient.delete()
                .uri(BASE_URI + "/" + VALID_TEAM_ID + "/competitions/" + NOT_FOUND_COMPETITION_ID)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void whenGetAllSponsorsClient_thenReturnsList() throws Exception {
        SponsorModel[] stub = new SponsorModel[]{
                SponsorModel.builder()
                        .sponsorId("s1")
                        .sponsorName("Acme")
                        .sponsorLevel("GOLD")
                        .sponsorAmount(10_000.0)
                        .build()
        };
        String json = mapper.writeValueAsString(stub);

        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        SponsorServiceClient client =
                new SponsorServiceClient(restTemplate, mapper, "localhost", "7002");

        List<SponsorModel> result = client.getAllSponsors();
        assertEquals(1, result.size());
        assertEquals("s1", result.get(0).getSponsorId());
        assertEquals("Acme", result.get(0).getSponsorName());

        mockServer.verify();
    }

    @Test
    void whenGetAllTeamsClient_thenReturnsList() throws Exception {
        TeamModel[] stub = new TeamModel[]{
                TeamModel.builder()
                        .teamId(VALID_TEAM_ID)
                        .teamName("Montreal Eagles")
                        .coachName("John Smith")
                        .teamLevel("COLLEGE")
                        .build()
        };
        String json = mapper.writeValueAsString(stub);

        mockServer.expect(once(),
                        requestTo(new URI(TEAM_SERVICE_BASE_URI)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        TeamServiceClient client =
                new TeamServiceClient(restTemplate, mapper, "localhost", "7001");

        List<TeamModel> result = client.getAllTeams();
        assertEquals(1, result.size());
        assertEquals(VALID_TEAM_ID, result.get(0).getTeamId());
        assertEquals("Montreal Eagles", result.get(0).getTeamName());

        mockServer.verify();
    }

    @Test
    void whenGetAllFacilitiesClient_thenReturnsList() throws Exception {
        FacilityModel[] stub = new FacilityModel[]{
                FacilityModel.builder()
                        .facilityId("fac1")
                        .facilityName("Stadium")
                        .capacity(50_000)
                        .location("City")
                        .build()
        };
        String json = mapper.writeValueAsString(stub);

        mockServer.expect(once(),
                        requestTo(new URI(FACILITY_SERVICE_BASE_URI)))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        FacilityServiceClient client =
                new FacilityServiceClient(restTemplate, mapper, "localhost", "7003");

        List<FacilityModel> result = client.getAllFacilities();
        assertEquals(1, result.size());
        assertEquals("fac1", result.get(0).getFacilityId());
        assertEquals("Stadium", result.get(0).getFacilityName());

        mockServer.verify();
    }
    @Test
    void whenPatchSponsorLevelNotFound_thenThrowsNotFoundException() throws Exception {
        String sponsorId = "s1";
        CompetitionHttpErrorInfo err = new CompetitionHttpErrorInfo(
                HttpStatus.NOT_FOUND,
                "/api/v1/sponsors/" + sponsorId + "/level",
                "Sponsor not found"
        );
        String body = mapper.writeValueAsString(err);

        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId + "/level")))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                );

        SponsorServiceClient client = new SponsorServiceClient(restTemplate, mapper, "localhost", "7002");

        assertThrows(NotFoundException.class,
                () -> client.patchSponsorLevelBySponsorId(sponsorId, "GOLD"));
        mockServer.verify();
    }

    @Test
    void whenPatchSponsorLevelUnprocessable_thenThrowsInvalidInputException() throws Exception {
        String sponsorId = "s1";
        CompetitionHttpErrorInfo err = new CompetitionHttpErrorInfo(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "/api/v1/sponsors/" + sponsorId + "/level",
                "Invalid level"
        );
        String body = mapper.writeValueAsString(err);

        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId + "/level")))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                );

        SponsorServiceClient client = new SponsorServiceClient(restTemplate, mapper, "localhost", "7002");

        assertThrows(InvalidInputException.class,
                () -> client.patchSponsorLevelBySponsorId(sponsorId, "INVALID"));
        mockServer.verify();
    }

    @Test
    void whenPatchSponsorLevelInvalidJson_thenThrowsRuntimeException() throws Exception {
        String sponsorId = "s1";
        mockServer.expect(once(),
                        requestTo(new URI(SPONSOR_SERVICE_BASE_URI + "/" + sponsorId + "/level")))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("this-is-not-json", MediaType.APPLICATION_JSON));

        SponsorServiceClient client = new SponsorServiceClient(restTemplate, mapper, "localhost", "7002");

        assertThrows(RuntimeException.class,
                () -> client.patchSponsorLevelBySponsorId(sponsorId, "GOLD"));
        mockServer.verify();
    }

}
