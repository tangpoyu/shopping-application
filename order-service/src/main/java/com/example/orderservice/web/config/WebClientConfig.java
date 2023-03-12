package com.example.orderservice.web.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig  {

    @Bean
    @LoadBalanced
    public WebClient.Builder appWebClientBuilder(){
        return WebClient.builder().exchangeStrategies(
                ExchangeStrategies.builder().codecs(
                        clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                ).build());
    }
}
