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
package com.main_group_ekn47.eventlib.consumer.idempotency;

import com.main_group_ekn47.eventlib.config.InfraProperties;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class ReactiveRedisIdempotencyStore implements IdempotencyStore {

    private final ReactiveStringRedisTemplate redis;
    private final String prefix;

    public ReactiveRedisIdempotencyStore(ReactiveStringRedisTemplate redis, InfraProperties infra) {
        this.redis = redis;
        this.prefix = infra.getRedis().getIdempotencyPrefix();
        System.out.print("!!!!!**********************************ReactiveStringRedisTemplate");
    }

    @Override
    public Mono<Boolean> isProcessed(String eventId) {
        return redis.hasKey(prefix + ":" + eventId)
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Void> markProcessed(String eventId) {
        return redis.opsForValue()
                .set(prefix + ":" + eventId, "PROCESSED", Duration.ofDays(1))
                .then();
    }
}