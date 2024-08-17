package tech.bjut.su.appeal.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tech.bjut.su.appeal.config.TestSecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class AdminControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithAnonymousUser
    public void testDeniesAnonymous() throws Exception {
        mvc.perform(get("/admin/admins"))
            .andExpect(status().is4xxClientError());
        mvc.perform(get("/admin/server/status"))
            .andExpect(status().is4xxClientError());
        mvc.perform(get("/admin/ip"))
            .andExpect(status().is4xxClientError());

        // test actuator permissions as well
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
        mvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @WithUserDetails
    public void testDeniesNonAdmin() throws Exception {
        mvc.perform(get("/admin/admins"))
            .andExpect(status().is4xxClientError());
        mvc.perform(get("/admin/server/status"))
            .andExpect(status().is4xxClientError());
        mvc.perform(get("/admin/ip"))
            .andExpect(status().is4xxClientError());

        // test actuator permissions as well
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
        mvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    @WithUserDetails(TestSecurityConfig.ADMIN_UID)
    public void testOkAdmin() throws Exception {
        mvc.perform(get("/admin/admins"))
            .andExpect(status().isOk());
        mvc.perform(get("/admin/server/status"))
            .andExpect(status().isOk());
        mvc.perform(get("/admin/ip"))
            .andExpect(status().isOk());

        // test actuator permissions as well
        mvc.perform(get("/actuator/health"))
            .andExpect(status().isOk());
        mvc.perform(get("/actuator/info"))
            .andExpect(status().isOk());
        mvc.perform(get("/actuator/metrics"))
            .andExpect(status().isOk());
    }
}
