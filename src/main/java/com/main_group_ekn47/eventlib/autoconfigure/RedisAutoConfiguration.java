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
import com.main_group_ekn47.eventlib.consumer.idempotency.IdempotencyStore;
import com.main_group_ekn47.eventlib.consumer.idempotency.ReactiveRedisIdempotencyStore;
//import com.main_group_ekn47.eventlib.consumer.idempotency.RedisIdempotencyStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;


import reactor.core.publisher.Mono;

@AutoConfiguration
public class RedisAutoConfiguration {

    // --- 1. INFRAESTRUCTURA BASE (CONEXIÓN) ---

    @Bean
    @ConditionalOnMissingBean(RedisConnectionFactory.class)
    public LettuceConnectionFactory redisConnectionFactory(InfraProperties infraProps) {
        var redisConfig = infraProps.getRedis();
        var config = new RedisStandaloneConfiguration(redisConfig.getHost(), redisConfig.getPort());

        if (redisConfig.getPassword() != null && !redisConfig.getPassword().isBlank()) {
            config.setPassword(redisConfig.getPassword());
        }

        System.out.println("-----> eventlib: Creando ConnectionFactory en " + redisConfig.getHost() + ":" + redisConfig.getPort());
        return new LettuceConnectionFactory(config);
    }

    // --- 2. CONFIGURACIÓN REACTIVA ---

    @Configuration
    @ConditionalOnClass({ReactiveStringRedisTemplate.class, ReactiveRedisConnectionFactory.class})
    static class ReactiveConfig {

        @Bean
        @ConditionalOnMissingBean(ReactiveStringRedisTemplate.class)
        public ReactiveStringRedisTemplate reactiveStringRedisTemplate(ReactiveRedisConnectionFactory factory) {
            return new ReactiveStringRedisTemplate(factory);
        }

        @Bean
        @ConditionalOnMissingBean(IdempotencyStore.class)
        public IdempotencyStore reactiveIdempotencyStore(
                ReactiveStringRedisTemplate reactiveTemplate,
                InfraProperties props) {
            System.out.println("-----> eventlib: Configurando Idempotencia REACTIVA (Redis)");
            return new ReactiveRedisIdempotencyStore(reactiveTemplate, props);
        }
    }

    // --- 3. CONFIGURACIÓN ESTÁNDAR ---
/*  @Configuration
    @ConditionalOnClass(StringRedisTemplate.class)
    static class StandardConfig {

        @Bean
        @ConditionalOnMissingBean(StringRedisTemplate.class)
        public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
            return new StringRedisTemplate(factory);
        }

        @Bean
        @ConditionalOnMissingBean(IdempotencyStore.class)
        public IdempotencyStore standardIdempotencyStore(
                StringRedisTemplate standardTemplate,
                InfraProperties props) {
            System.out.println("-----> eventlib: Configurando Idempotencia ESTÁNDAR (Redis)");
            return new RedisIdempotencyStore(standardTemplate, props);
        }
    }*/

    // --- 4. PLAN B: FALLBACK ---

    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    public IdempotencyStore noOpIdempotencyStore() {
        System.out.println("-----> eventlib: ALERTA - Usando Idempotencia NULA");
        return new IdempotencyStore() {
            @Override
            public Mono<Boolean> isProcessed(String eventId) { return Mono.just(false); }
            @Override
            public Mono<Void> markProcessed(String eventId) { return Mono.empty(); }
        };
    }
}