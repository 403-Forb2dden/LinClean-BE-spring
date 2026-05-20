package com.linclean.security;

import com.linclean.domain.member.entity.Member;
import com.linclean.domain.member.exception.WithdrawnMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ClerkJwtAuthenticationConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    private final MemberSyncService memberSyncService;

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        Member member;
        try {
            member = memberSyncService.findOrCreate(jwt.getSubject());
        } catch (WithdrawnMemberException e) {
            throw new BadCredentialsException(e.getMessage());
        }
        MemberPrincipal principal = new MemberPrincipal(member.getId(), member.getPublicId());
        return new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }
}
