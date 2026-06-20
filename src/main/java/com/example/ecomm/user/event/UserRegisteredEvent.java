package com.example.ecomm.user.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final String userId;
    private final String email;
    private final String firstName;

    public UserRegisteredEvent(Object source, String userId, String email, String firstName) {
        super(source);
        this.userId    = userId;
        this.email     = email;
        this.firstName = firstName;
    }
}
