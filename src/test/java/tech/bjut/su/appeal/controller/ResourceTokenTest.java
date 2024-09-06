package tech.bjut.su.appeal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.bjut.su.appeal.config.TestSecurityConfig;
import tech.bjut.su.appeal.dto.QuestionCreateDto;
import tech.bjut.su.appeal.entity.Question;
import tech.bjut.su.appeal.enums.CampusEnum;
import tech.bjut.su.appeal.repository.QuestionRepository;
import tech.bjut.su.appeal.security.JwtResourceAuthoritiesHelper;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class ResourceTokenTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QuestionRepository questionRepository;

    // the test data
    private static final CampusEnum QUESTION_CAMPUS = CampusEnum.MAIN;
    private static final String QUESTION_CONTENT = "Content";

    @Test
    public void testQuestionSubmit_returnsToken() throws Exception {
        QuestionCreateDto dto = new QuestionCreateDto();
        dto.setUid("");
        dto.setName("");
        dto.setCampus(QUESTION_CAMPUS);
        dto.setContent(QUESTION_CONTENT);

        String response = mvc.perform(post("/questions")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsBytes(dto)))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(response, "$.resource_token.access_token");
        long id = ((Number) JsonPath.read(response, "$.data.id")).longValue();

        mvc.perform(get("/questions/" + id)
            .header("Authorization", "Bearer " + token))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.id").value(id)
            );

        mvc.perform(get("/questions/history")
            .header("Authorization", "Bearer " + token))
            .andExpectAll(
                status().isOk(),
                jsonPath("$.data[0].id").value(id)
            );
    }

    @Test
    public void testQuestionQuery_fakeToken() throws Exception {
        // setup
        Question question = new Question();
        question.setUser(null);
        question.setCampus(QUESTION_CAMPUS);
        question.setContent(QUESTION_CONTENT);
        question.setPublished(false);
        question = questionRepository.save(question);

        // build fake token
        JWKSource<SecurityContext> jwkSource = new ImmutableSecret<>(new byte[32]);
        JwtEncoder encoder = new NimbusJwtEncoder(jwkSource);
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuedAt(Instant.now().minusSeconds(60))
            .expiresAt(Instant.now().plusSeconds(86400))
            .claim(
                JwtResourceAuthoritiesHelper.CLAIM_RESOURCES,
                new String[]{"question," + question.getId()}
            )
            .build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        // verify
        mvc.perform(get("/questions/" + question.getId())
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());

        mvc.perform(get("/questions/history")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isUnauthorized());
    }
}
