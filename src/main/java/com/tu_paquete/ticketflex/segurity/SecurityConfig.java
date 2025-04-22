package com.tu_paquete.ticketflex.segurity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    
    private final CustomLoginSuccessHandler successHandler;

    public SecurityConfig(UserDetailsService userDetailsService, CustomLoginSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF (solo para desarrollo)
            .authorizeHttpRequests(auth -> auth
            	.requestMatchers("/", "/index", "/public/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/admin/**").hasRole("Administrador") // Solo "Administrador" puede acceder a /admin/**
                .requestMatchers("/api/eventos/listar").permitAll() // Permitir sin autenticación
                .requestMatchers("/api/eventos/filtrar").permitAll()
                .requestMatchers("/api/usuarios/login").permitAll()

                .anyRequest().authenticated() // Todas las demás rutas requieren autenticación
            )
            .formLogin(form -> form
            	    .loginPage("/login")
            	    .loginProcessingUrl("/login")
            	    .successHandler(successHandler) // Usa el handler personalizado
            	    .failureUrl("/login?error=true")
            	    .permitAll()
            	)

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
             )
            .exceptionHandling(exception -> exception
                    .accessDeniedPage("/access-denied") // Nueva ruta para acceso denegado
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}