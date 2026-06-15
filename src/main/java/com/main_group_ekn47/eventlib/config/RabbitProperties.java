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

import java.util.HashMap;
import java.util.Map;

/**
 * Configuración declarativa de RabbitMQ.
 *
 * eventlib.infra.rabbitmq.*
 */
public class RabbitProperties {

    /**
     * Exchange principal (fanout / topic).
     */
    private String exchange = "eventlib.exchange";

    /**
     * Map<RoutingKey, QueueName>
     *
     * Ej:
     * user-created-notification -> notification.user.created.queue
     */
    private Map<String, String> queues = new HashMap<>();

    private DlqProperties dlq = new DlqProperties();

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public Map<String, String> getQueues() {
        return queues;
    }

    public void setQueues(Map<String, String> queues) {
        this.queues = queues;
    }

    public DlqProperties getDlq() {
        return dlq;
    }

    public void setDlq(DlqProperties dlq) {
        this.dlq = dlq;
    }

    public static class DlqProperties {

        /**
         * Sufijo estándar de DLQ.
         */
        private String suffix = ".dlq";

        public String getSuffix() {
            System.out.print("!!!!!**********************************RabbitProperties");

            return suffix;
        }

        public void setSuffix(String suffix) {
            this.suffix = suffix;
        }
    }
}
