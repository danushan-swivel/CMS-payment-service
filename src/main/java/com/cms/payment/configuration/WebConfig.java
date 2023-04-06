package com.cms.payment.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class WebConfig {
    private final String key;
    private static final String[] AUTH_WHITE_LIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/v2/api-docs/**",
            "/swagger-resources/**"
    };

    public WebConfig(@Value("${security.key}") String key) {
        this.key = key;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(new JwtValidator(key), BasicAuthenticationFilter.class)
                .csrf().disable()
                .authorizeRequests().antMatchers(AUTH_WHITE_LIST).permitAll()
                .anyRequest().authenticated().and()
                .formLogin().and()
                .httpBasic();
        return http.build();
    }
}
