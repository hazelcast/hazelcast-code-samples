/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.samples;

import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private HazelcastIndexedSessionRepository sessionRepository;

    @Bean
    public SpringSessionBackedSessionRegistry<HazelcastIndexedSessionRepository.HazelcastSession> sessionRegistry() {
        return new SpringSessionBackedSessionRegistry<>(this.sessionRepository);
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
         http
                .authorizeHttpRequests((requests) -> requests
                                               .anyRequest().authenticated()
                                      )
                .httpBasic(withDefaults())
                // other config goes here...
                .sessionManagement((sessionManagement) -> sessionManagement
                                           .maximumSessions(2)
                                           .sessionRegistry(sessionRegistry())
                                  )
                .logout(LogoutConfigurer::permitAll);

        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.inMemoryAuthentication()
            .withUser(User.withUsername("user").password("{noop}password").roles("USER").build())
            .withUser(User.withUsername("user2").password("{noop}password2").roles("USER").build())
        ;
    }
}
