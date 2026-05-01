package com.linclean.domain.analysis.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Stages {

    private ExternalDbStage externalDb;
    private UnchainStage unchain;
    private DomainHeuristicStage domainHeuristic;
    private ContentAnalysisStage contentAnalysis;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExternalDbStage {
        private GsbResult gsb;
        private UrlHausResult urlhaus;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class GsbResult {
            @JsonProperty("isThreat")
            private boolean isThreat;
            private List<String> matchedTypes;
        }

        @Getter
        @Setter
        @NoArgsConstructor
        public static class UrlHausResult {
            @JsonProperty("isThreat")
            private boolean isThreat;
            private String host;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UnchainStage {
        private int hops;
        private List<String> chain;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class DomainHeuristicStage {
        private RdapInfo rdap;
        private List<String> signals;

        @Getter
        @Setter
        @NoArgsConstructor
        public static class RdapInfo {
            private String domain;
            private String registrar;
            private String createdDate;
            private int domainAgeDays;
            @JsonProperty("isNewDomain")
            private boolean isNewDomain;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ContentAnalysisStage {
        private boolean fetched;
        private boolean hasPasswordField;
        private String aiVerdict;
        private String aiReason;
    }
}
