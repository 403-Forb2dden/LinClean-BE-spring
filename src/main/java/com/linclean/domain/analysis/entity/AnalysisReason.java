package com.linclean.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "analysis_reason")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AnalysisReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private Analysis analysis;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "stage", nullable = false)
    private int stage;

    @Column(name = "weight", nullable = false)
    private int weight;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
}
