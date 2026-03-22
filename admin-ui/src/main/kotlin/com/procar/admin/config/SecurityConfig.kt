package com.procar.admin.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/VAADIN/**", "/frontend/**", "/images/**", "/icons/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin { form ->
                form
                    .defaultSuccessUrl("/", true)
            }
            .logout { logout ->
                logout
                    .logoutSuccessUrl("/login?logout")
                    .permitAll()
            }
            .csrf { csrf -> 
                csrf.disable() // Disable CSRF for simplicity in admin UI
            }
        return http.build()
    }

    @Bean
    fun userDetailsService(passwordEncoder: PasswordEncoder): UserDetailsService {
        val admin: UserDetails = User.builder()
            .username("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN")
            .build()

        return InMemoryUserDetailsManager(admin)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
