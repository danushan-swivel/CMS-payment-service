package com.cms.payment.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Collections;

@EnableWebSecurity
@Configuration
public class WebConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .addFilterBefore(new JwtValidator(), BasicAuthenticationFilter.class)
                .cors().configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowCredentials(true);
                    configuration.setAllowedHeaders(Collections.singletonList("*"));
                    configuration.setAllowedMethods(Collections.singletonList("*"));
                    configuration.setExposedHeaders(Collections.singletonList("*"));
                    configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000/"));
                    configuration.setMaxAge(3600L);
                    return configuration;
                }).and()
                .authorizeRequests(
                        authorize -> authorize.anyRequest().authenticated()
                ).formLogin().and()
                .httpBasic();
    }
}
