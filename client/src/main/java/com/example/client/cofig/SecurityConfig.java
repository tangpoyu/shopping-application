package com.example.client.cofig;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig{

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${logout.url}")
    private String logoutUrl;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/home").permitAll()
                        .requestMatchers("/logout").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2->oauth2.userInfoEndpoint(
                        userInfoEndpointConfig -> userInfoEndpointConfig.oidcUserService(this.oidcUserService())
                ))
                .logout(logout-> logout.logoutSuccessHandler(logoutSuccessHandler(clientRegistrationRepository)))
                .httpBasic(withDefaults());

        return http.build();
    }

    // used to customize the way we use to extract principal and authorities.
    private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        final OidcUserService delegate = new OidcUserService();

        return (userRequest) -> {
            // Delegate to the default implementation for loading a user
            OidcUser oidcUser = delegate.loadUser(userRequest);

            OAuth2AccessToken accessToken = userRequest.getAccessToken();
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();


            try{
                // 1) Fetch the authority information from the protected resource using accessToken
                LinkedTreeMap resource_access = (LinkedTreeMap) oidcUser.getAttributes().get("resource_access");
                LinkedTreeMap apiGateway  = (LinkedTreeMap) resource_access.get(serviceName);
                ArrayList roles = (ArrayList) apiGateway.get("roles");
                // 2) Map the authority information to one or more GrantedAuthority's and add it to mappedAuthorities
                roles.forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority(role.toString())));
            } catch (Exception e){
                mappedAuthorities.add(new SimpleGrantedAuthority("user"));
            }
            // 3) Create a copy of oidcUser but use the mappedAuthorities instead
            oidcUser = new DefaultOidcUser(mappedAuthorities, oidcUser.getIdToken(), oidcUser.getUserInfo());

            return oidcUser;
        };
    }

    // used to handle logout with authorization server
    private LogoutSuccessHandler logoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository){
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri(String.valueOf(URI.create(logoutUrl)));
        return oidcLogoutSuccessHandler;
    }

    // used to use webclient call service with access token
    @Bean
    @LoadBalanced
    WebClient.Builder webClient(ClientRegistrationRepository clientRegistrationRepository,
                                OAuth2AuthorizedClientRepository auth2AuthorizedClientRepository){

        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrationRepository,
                        auth2AuthorizedClientRepository);


        oauth2.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder().baseUrl("http://API-GATEWAY").apply(oauth2.oauth2Configuration())
                .exchangeStrategies(ExchangeStrategies.builder().codecs(
                        configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)
                ).build());
    }
}


