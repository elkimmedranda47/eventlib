/*
 * Licensed to Elkim Andres Medranda Caicedo under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Elkim Andres Medranda Caicedo licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.main_group_ekn47.eventlib.broker;
/*
public interface MessagePublisher {

    void publish(
            String exchange,
            String routingKey,
            byte[] payload
    );
    // NUEVO: Método para enviar strings directamente (soluciona el error)
    void publishRaw(String topic, String payload);
}
*/

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

public interface MessagePublisher {
    Mono<Void> publish(String exchange, String routingKey, byte[] payload);

    Mono<Void> publishRaw(String topic, String payload);

    // NUEVO: Este método permite pasar el Objeto directamente desde el Handler
    default Mono<Void> publishEvent(String topic, Object event) {

        try {
            // Usamos una instancia básica de ObjectMapper para la conversión
            ObjectMapper mapper = new ObjectMapper();
            mapper.findAndRegisterModules(); // Para soportar Java 8 dates si las usas
            String json = mapper.writeValueAsString(event);
            return publishRaw(topic, json);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Error serializando evento", e));
        }

    }
}