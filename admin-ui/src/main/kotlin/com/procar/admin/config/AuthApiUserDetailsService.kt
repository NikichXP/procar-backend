package com.procar.admin.config

import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AuthApiUserDetailsService : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        // Return a dummy user with placeholder password
        // The actual authentication happens in AuthApiAuthenticationProvider
        return User.builder()
            .username(username)
            .password("DUMMY_PASSWORD") // Placeholder password, not used for validation
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
            .build()
    }
}
