package com.linclean.domain.analysis.controller;

import com.linclean.domain.analysis.dto.response.VerdictStatisticsResponse;
import com.linclean.domain.analysis.entity.Verdict;
import com.linclean.domain.analysis.service.AnalysisService;
import com.linclean.domain.analysis.service.VerdictStatisticsService;
import com.linclean.security.MemberSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        value = AnalysisController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class}
)
class VerdictStatisticsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean VerdictStatisticsService verdictStatisticsService;
    @MockBean AnalysisService analysisService;
    @MockBean MemberSyncService memberSyncService;

    @Test
    void getVerdictStatistics_returns200WithCounts() throws Exception {
        List<Object[]> dbResult = List.<Object[]>of(
                new Object[]{Verdict.SAFE, 100L},
                new Object[]{Verdict.CAUTION, 20L},
                new Object[]{Verdict.DANGER, 5L}
        );
        given(verdictStatisticsService.getVerdictStatistics())
                .willReturn(VerdictStatisticsResponse.from(dbResult));

        mockMvc.perform(get("/api/v1/analyses/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.safe").value(100))
                .andExpect(jsonPath("$.data.caution").value(20))
                .andExpect(jsonPath("$.data.danger").value(5));
    }
}
