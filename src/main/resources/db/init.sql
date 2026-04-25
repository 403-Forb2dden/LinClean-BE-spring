-- ============================================================
-- LinClean ERD - PostgreSQL 17 DDL
-- ============================================================

-- UUID 생성용 확장 모듈
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================
-- 1. MEMBER
-- ============================================================
CREATE TABLE member (
                        id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        public_id       UUID            NOT NULL DEFAULT gen_random_uuid(),
                        kakao_id        VARCHAR(255)    NOT NULL,
                        created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
                        updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
                        deleted_at      TIMESTAMPTZ,

                        CONSTRAINT uq_member_public_id  UNIQUE (public_id)
);

-- ============================================================
-- 2. DEVICE_TOKEN
-- ============================================================
CREATE TABLE device_token (
                              id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              member_id       BIGINT          NOT NULL,
                              fcm_token       VARCHAR(512)    NOT NULL,
                              device_type     VARCHAR(20)     NOT NULL,
                              created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
                              updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

                              CONSTRAINT uq_device_token_fcm  UNIQUE (fcm_token),
                              CONSTRAINT fk_device_token_member
                                  FOREIGN KEY (member_id) REFERENCES member (id)
                                      ON DELETE CASCADE
);

CREATE INDEX idx_device_token_member_id ON device_token (member_id);

-- ============================================================
-- 3. NOTIFICATION
-- ============================================================
CREATE TABLE notification (
                              id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                              member_id       BIGINT          NOT NULL,
                              type            VARCHAR(50)     NOT NULL,
                              title           VARCHAR(255)    NOT NULL,
                              body            TEXT,
                              target_url      VARCHAR(2048),
                              is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
                              created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

                              CONSTRAINT fk_notification_member
                                  FOREIGN KEY (member_id) REFERENCES member (id)
                                      ON DELETE CASCADE
);

CREATE INDEX idx_notification_member_id ON notification (member_id);
CREATE INDEX idx_notification_member_read ON notification (member_id, is_read);

-- ============================================================
-- 4. ANALYSIS
-- ============================================================
CREATE TABLE analysis (
                          analysis_id     UUID            PRIMARY KEY,
                          member_id       BIGINT          NOT NULL,
                          original_url    VARCHAR(2048)   NOT NULL,
                          final_url       VARCHAR(2048),
                          status          VARCHAR(20)     NOT NULL DEFAULT 'queued',
                          verdict         VARCHAR(20),
                          score           INTEGER,
                          summary         TEXT,
                          stages          JSONB,
                          error_code      VARCHAR(100),
                          error_stage     INTEGER,
                          error_message   TEXT,
                          engine_version  VARCHAR(50),
                          analyzed_at     TIMESTAMPTZ,
                          elapsed_ms      INTEGER,
                          request_id      UUID,
                          last_checked_at TIMESTAMPTZ,
                          created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
                          updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

                          CONSTRAINT fk_analysis_member
                              FOREIGN KEY (member_id) REFERENCES member (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT chk_analysis_status
                              CHECK (status IN ('queued', 'succeeded', 'failed')),
                          CONSTRAINT chk_analysis_verdict
                              CHECK (verdict IS NULL OR verdict IN ('safe', 'caution', 'danger')),
                          CONSTRAINT chk_analysis_score
                              CHECK (score IS NULL OR score BETWEEN 0 AND 100)
);

CREATE INDEX idx_analysis_member_id ON analysis (member_id);
CREATE INDEX idx_analysis_status ON analysis (status);
CREATE INDEX idx_analysis_member_status ON analysis (member_id, status);
CREATE INDEX idx_analysis_last_checked ON analysis (last_checked_at);

-- ============================================================
-- 5. ANALYSIS_REASON
-- ============================================================
CREATE TABLE analysis_reason (
                                 id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 analysis_id     UUID            NOT NULL,
                                 code            VARCHAR(100)    NOT NULL,
                                 stage           INTEGER         NOT NULL,
                                 weight          INTEGER         NOT NULL,
                                 message         TEXT            NOT NULL,

                                 CONSTRAINT fk_reason_analysis
                                     FOREIGN KEY (analysis_id) REFERENCES analysis (analysis_id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT chk_reason_stage
                                     CHECK (stage BETWEEN 1 AND 4)
);

CREATE INDEX idx_analysis_reason_analysis_id ON analysis_reason (analysis_id);

-- ============================================================
-- 6. CATEGORY
-- ============================================================
CREATE TABLE category (
                          id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                          member_id       BIGINT          NOT NULL,
                          name            VARCHAR(50)     NOT NULL,
                          display_order   INTEGER         NOT NULL DEFAULT 0,
                          created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

                          CONSTRAINT fk_category_member
                              FOREIGN KEY (member_id) REFERENCES member (id)
                                  ON DELETE CASCADE,

                          CONSTRAINT uq_category_member_name
                              UNIQUE (member_id, name)
);

CREATE INDEX idx_category_member_id ON category (member_id);

-- ============================================================
-- 7. SAVED_LINK
-- ============================================================
CREATE TABLE saved_link (
                            id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                            member_id       BIGINT          NOT NULL,
                            analysis_id     UUID            NOT NULL,
                            category_id     BIGINT,
                            original_url    VARCHAR(2048)   NOT NULL,
                            final_url       VARCHAR(2048),
                            title           VARCHAR(500),
                            description     TEXT,
                            site_name       VARCHAR(255),
                            is_bookmarked   BOOLEAN         NOT NULL DEFAULT FALSE,
                            created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
                            updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),

                            CONSTRAINT fk_saved_link_member
                                FOREIGN KEY (member_id) REFERENCES member (id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_saved_link_analysis
                                FOREIGN KEY (analysis_id) REFERENCES analysis (analysis_id)
                                    ON DELETE CASCADE,
                            CONSTRAINT fk_saved_link_category
                                FOREIGN KEY (category_id) REFERENCES category (id)
                                    ON DELETE SET NULL
);

CREATE INDEX idx_saved_link_member_id ON saved_link (member_id);
CREATE INDEX idx_saved_link_analysis_id ON saved_link (analysis_id);
CREATE INDEX idx_saved_link_category_id ON saved_link (category_id);
CREATE INDEX idx_saved_link_member_bookmark ON saved_link (member_id, is_bookmarked);

-- ============================================================
-- 8. NOTICE
-- ============================================================
CREATE TABLE notice (
                        id              BIGINT          GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        title           VARCHAR(255)    NOT NULL,
                        content         TEXT            NOT NULL,
                        is_pinned       BOOLEAN         NOT NULL DEFAULT FALSE,
                        created_at      TIMESTAMPTZ     NOT NULL DEFAULT now(),
                        updated_at      TIMESTAMPTZ     NOT NULL DEFAULT now()
);

-- ============================================================
-- 활성 회원 기준 kakao_id 유니크 인덱스 (소프트 삭제 고려)
-- 탈퇴 후 재가입 시 동일 kakao_id로 새 row 삽입 가능
-- ============================================================
CREATE UNIQUE INDEX uq_member_kakao_id_active
    ON member (kakao_id) WHERE deleted_at IS NULL;