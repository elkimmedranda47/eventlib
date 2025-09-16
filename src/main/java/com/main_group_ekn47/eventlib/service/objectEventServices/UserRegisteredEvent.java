package com.main_group_ekn47.eventlib.service.objectEventServices;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.main_group_ekn47.eventlib.core.IntegrationEvent;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Ignora cualquier campo en el JSON que no esté en la clase Java
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
public class UserRegisteredEvent extends IntegrationEvent {

    private String userId;

    // Usa @JsonProperty para mapear el campo 'email' del JSON a 'userEmail' en tu clase
    @JsonProperty("email")
    private String userEmail;
    // private String activationToken; // Campo para el token de activación

    public UserRegisteredEvent(String userId, String userEmail/*, String activationToken*/) {
        super(userId);
        this.userId = userId;
        this.userEmail = userEmail;
        // this.activationToken = activationToken;
    }
}