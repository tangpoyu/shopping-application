package com.example.inventoryservice.web.config;


import lombok.extern.log4j.Log4j2;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;

import java.util.*;

@Log4j2
class CustomAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private String principalClaimName = "sub";

    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        try {
            Map resource_access = jwt.getClaim("resource_access");
            Map browser_client = (Map) resource_access.get("browser-client");
            ArrayList roles = (ArrayList) browser_client.get("roles");
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.toString())));
        } catch (Exception e){
            authorities.add(new SimpleGrantedAuthority("user"));
        }

        try{
            Object scope = jwt.getClaim("scope");
            if(scope instanceof String){
                Collection<String> granted_scope = StringUtils.hasText((String) scope) ?
                        List.of(((String) scope).split(" ")) : Collections.emptyList();
                granted_scope.forEach(s -> authorities.add(new SimpleGrantedAuthority("Scope_" + s)));
            }
        }catch (Exception e){
            log.info("No scope is granted with this client");
        }
        String principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        return new JwtAuthenticationToken(jwt,authorities,principalClaimValue);
    }
}