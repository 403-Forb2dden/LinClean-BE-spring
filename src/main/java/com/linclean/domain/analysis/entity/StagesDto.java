package com.linclean.domain.analysis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StagesDto {

    private ExternalDbStage externalDb;
    private UnchainStage unchain;
    private DomainHeuristicStage domainHeuristic;
    private ContentAnalysisStage contentAnalysis;

    @Getter
    @NoArgsConstructor
    public static class ExternalDbStage {
        private GsbResult gsb;
        private UrlHausResult urlhaus;

        @Getter
        @NoArgsConstructor
        public static class GsbResult {
            private boolean isThreat;
            private List<String> matchedTypes;
        }

        @Getter
        @NoArgsConstructor
        public static class UrlHausResult {
            private boolean isThreat;
            private String host;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class UnchainStage {
        private int hops;
        private List<String> chain;
    }

    @Getter
    @NoArgsConstructor
    public static class DomainHeuristicStage {
        private RdapInfo rdap;
        private List<String> signals;

        @Getter
        @NoArgsConstructor
        public static class RdapInfo {
            private String domain;
            private String registrar;
            private String createdDate;
            private int domainAgeDays;
            private boolean isNewDomain;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class ContentAnalysisStage {
        private boolean fetched;
        private boolean hasPasswordField;
        private String aiVerdict;
        private String aiReason;
    }
}
