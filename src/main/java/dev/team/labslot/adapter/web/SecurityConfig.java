package dev.team.labslot.adapter.web;

import dev.team.labslot.core.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad de las vistas: login con formulario, roles y rutas protegidas.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Login, archivos estáticos y páginas de error son públicos
                        .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // El administrador no reserva: si entra aquí recibe la página 403
                        .requestMatchers("/reservations/**").hasRole("USER")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }

    // Las contraseñas de los usuarios están guardadas como hashes BCrypt
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security busca al usuario por el email escrito en el login.
    // El rol (ROLE_ADMIN o ROLE_USER) ya trae el prefijo ROLE_, por eso se usa authorities y no roles.
    @Bean
    public UserDetailsService userDetailsService(UserService userService) {
        return email -> userService.findByEmail(email)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.email())
                        .password(user.password())
                        .authorities(user.role().name())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con ese correo"));
    }
}
