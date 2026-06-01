package com.linclean.domain.member.exception;

public class WithdrawnMemberException extends RuntimeException {
    public WithdrawnMemberException() {
        super("탈퇴한 회원입니다.");
    }
}
