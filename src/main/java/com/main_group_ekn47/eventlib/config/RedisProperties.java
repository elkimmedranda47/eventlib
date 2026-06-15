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
package com.main_group_ekn47.eventlib.config;

/**
 * Configuración Redis para infraestructura y gestión de idempotencia.
 * Estos valores pueden ser sobrescritos en el application.properties del microservicio
 * usando el prefijo: eventlib.infra.redis.*
 */
public class RedisProperties {

    /**
     * Host del servidor Redis. Por defecto: localhost
     */
    private String host = "localhost";

    /**
     * Puerto del servidor Redis. Por defecto: 6379
     */
    private int port = 6379;

    /**
     * Contraseña del servidor Redis (opcional).
     */
    private String password;

    /**
     * Prefijo para las claves almacenadas en Redis para evitar colisiones.
     * Ej: eventlib:idempotency:email-service:
     */
    private String idempotencyPrefix = "eventlib:idempotency";

    // --- Getters y Setters ---

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdempotencyPrefix() {
        // Mantenemos tu print de depuración para que veas cuándo la librería accede a la propiedad
        System.out.println("!!!!!********************************** Accediendo a RedisProperties");
        return idempotencyPrefix;
    }

    public void setIdempotencyPrefix(String idempotencyPrefix) {
        this.idempotencyPrefix = idempotencyPrefix;
    }
}
