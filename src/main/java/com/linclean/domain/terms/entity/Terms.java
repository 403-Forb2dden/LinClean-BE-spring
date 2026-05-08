package com.linclean.domain.terms.entity;

import com.linclean.domain.terms.converter.ContentFormatConverter;
import com.linclean.domain.terms.converter.TermsTypeConverter;
import com.linclean.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Entity
@Table(name = "terms")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Terms extends BaseEntity {

    @Convert(converter = TermsTypeConverter.class)
    @Column(name = "type", nullable = false, unique = true, length = 30)
    private TermsType type;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Convert(converter = ContentFormatConverter.class)
    @Column(name = "content_format", nullable = false, length = 20)
    private ContentFormat contentFormat;

    @Column(name = "effective_at", nullable = false)
    private Instant effectiveAt;
}