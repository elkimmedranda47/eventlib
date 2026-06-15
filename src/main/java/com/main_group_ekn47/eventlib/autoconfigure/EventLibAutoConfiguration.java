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
package com.main_group_ekn47.eventlib.autoconfigure;

import com.main_group_ekn47.eventlib.config.InfraProperties;
import com.main_group_ekn47.eventlib.core.MessageSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuración base de EventLib.
 *
 * Esta clase:
 * - Registra las ConfigurationProperties
 * - Habilita el uso de eventlib.* en el Environment
 * - NO crea beans
 * - NO activa infra por sí sola
 */
@AutoConfiguration
@EnableConfigurationProperties(InfraProperties.class)
public class EventLibAutoConfiguration {
    // Punto de entrada del starter (intencionalmente vacío)
    @Bean
    @ConditionalOnMissingBean
    public MessageSerializer messageSerializer() {
        return new MessageSerializer();
    }
}
