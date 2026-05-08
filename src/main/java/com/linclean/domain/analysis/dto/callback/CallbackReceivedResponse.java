package com.linclean.domain.analysis.dto.callback;

public record CallbackReceivedResponse(boolean received) {
    public static CallbackReceivedResponse ok() {
        return new CallbackReceivedResponse(true);
    }
}
