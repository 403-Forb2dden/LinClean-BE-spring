package com.linclean.domain.terms.entity;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class TermsTypeTest {

    @Test
    void from_validValues() {
        assertThat(TermsType.from("terms_of_service")).isEqualTo(TermsType.TERMS_OF_SERVICE);
        assertThat(TermsType.from("privacy_policy")).isEqualTo(TermsType.PRIVACY_POLICY);
        assertThat(TermsType.from("service_guide")).isEqualTo(TermsType.SERVICE_GUIDE);
    }

    @Test
    void from_invalidValue_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> TermsType.from("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown TermsType");
    }

    @Test
    void getValue_returnsSnakeCaseString() {
        assertThat(TermsType.TERMS_OF_SERVICE.getValue()).isEqualTo("terms_of_service");
        assertThat(TermsType.PRIVACY_POLICY.getValue()).isEqualTo("privacy_policy");
        assertThat(TermsType.SERVICE_GUIDE.getValue()).isEqualTo("service_guide");
    }
}