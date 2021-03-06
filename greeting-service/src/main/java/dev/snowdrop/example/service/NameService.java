/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.snowdrop.example.service;

import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service invoking name-service via REST and guarded by Hystrix.
 */
@Service
public class NameService {

    private static final HystrixCommandKey KEY = HystrixCommandKey.Factory.asKey("NameService");

    private final String nameHost = System.getProperty("name.host", "http://spring-boot-circuit-breaker-name:8080");
    private final RestTemplate restTemplate = new RestTemplate();

    @HystrixCommand(commandKey = "NameService", fallbackMethod = "getFallbackName", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000")
    })
    public String getName() {
        return restTemplate.getForObject(nameHost + "/api/name", String.class);
    }

    private String getFallbackName() {
        return "Fallback";
    }

    CircuitBreakerState getState() throws Exception {
        HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(KEY);
        return circuitBreaker != null && circuitBreaker.isOpen() ? CircuitBreakerState.OPEN : CircuitBreakerState.CLOSED;
    }
}
