package com.linclean.auth.jwt;

import java.util.UUID;

public record MemberPrincipal(Long memberId, UUID publicId) {}
