package com.main_group_ekn47.eventlib.service.eventObjectDto;

import com.main_group_ekn47.eventlib.core.IntegrationEvent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor
public class UserRegisteredEvent extends IntegrationEvent {
//ServiceThreeUserRegisteredEvent

    private String userId;
    private String userEmail;
    private String activationToken; // Campo para el token de activación

    public UserRegisteredEvent(String userId, String userEmail, String activationToken) {
        super(userId);
        this.userId = userId;
        this.userEmail = userEmail;
        this.activationToken = activationToken;
    }
}
