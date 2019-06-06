package it.polito.ai.mmap.pedibus.configuration;

import it.polito.ai.mmap.pedibus.services.JwtTokenService;
import it.polito.ai.mmap.pedibus.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    UserService userService;

    @Autowired
    JwtTokenService jwtTokenService;


    /**
     * Si specifica come si accede alle risorse
     *
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/images/**").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/css/**").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/js/**").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/register").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/confirm/**").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/recover/**").permitAll()
                .and()
                .authorizeRequests()//todo delete
                .antMatchers("/reservations/**").permitAll()
                .and()
                .authorizeRequests()//todo delete
                .antMatchers("/lines/**").permitAll()
                .and()
                .authorizeRequests()
                .antMatchers("/users/**").access("hasAnyRole('ADMIN','SYSTEM-ADMIN')")
                .and()
                .csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and().httpBasic()
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and().apply(new JwtConfigurer(jwtTokenService));
    }

    /**
     * Invece di usare il service standard usiamo un UserDetailsService da noi definito
     *
     * @param builder
     * @throws Exception
     */
    @Override
    public void configure(AuthenticationManagerBuilder builder) throws Exception {
        builder.userDetailsService(userService).passwordEncoder(passwordEncoder());
        builder.inMemoryAuthentication()
                .withUser("userAd")
                .password(passwordEncoder().encode("pass"))
                .roles("admin")
                .and()
                .withUser("user")
                .password(passwordEncoder().encode("pass"))
                .roles("user");
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}