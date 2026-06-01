package com.linclean.security;

import java.util.UUID;

public record MemberPrincipal(Long memberId, UUID publicId) {}
