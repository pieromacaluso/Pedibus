package it.polito.ai.mmap.esercitazione3.configuration;

import it.polito.ai.mmap.esercitazione3.filter.JwtTokenFilter;
import it.polito.ai.mmap.esercitazione3.services.JwtTokenService;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private JwtTokenService jwtTokenService;
    public JwtConfigurer(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }
    @Override
    public void configure(HttpSecurity http) throws Exception {
        JwtTokenFilter customFilter = new JwtTokenFilter(jwtTokenService);
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}