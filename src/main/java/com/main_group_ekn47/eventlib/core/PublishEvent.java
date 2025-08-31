package com.main_group_ekn47.eventlib.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/* **********************************************************************************************************
*   LA ANOTACIÓN  @PublishEvent(topic = "user-events", eventName = "UserRegisteredEvent") DE LOS PRODUCER   *
*********************************************************************************************************** */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PublishEvent {
    String topic();
    String eventName();
}

/*La función principal de la anotación @PublishEvent es actuar como una etiqueta o marcador para los métodos.
Por sí misma, no realiza ninguna acción.

Su propósito es proporcionar metadatos sobre un evento que debe ser publicado, permitiendo que otra parte
de la aplicación (como un aspecto de AOP o un proxy de Spring) intercepte la llamada a ese método y realice
la acción de publicación real.
*/