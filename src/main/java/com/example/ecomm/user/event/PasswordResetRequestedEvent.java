package com.example.ecomm.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class PasswordResetRequestedEvent extends ApplicationEvent {

    private final String email;
    private final String firstName;
    private final String resetToken;

    public PasswordResetRequestedEvent(Object source, String email, String firstName, String resetToken) {
        super(source);
        this.email      = email;
        this.firstName  = firstName;
        this.resetToken = resetToken;
    }
}
