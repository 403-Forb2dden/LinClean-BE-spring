package com.linclean.domain.device.entity;

import com.linclean.domain.device.converter.DeviceTypeConverter;
import com.linclean.global.entity.BaseEntity;
import com.linclean.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "device_token")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DeviceToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 512)
    private String fcmToken;

    @Convert(converter = DeviceTypeConverter.class)
    @Column(name = "device_type", nullable = false, length = 20)
    private DeviceType deviceType;
}
