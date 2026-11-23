package br.com.medicare.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Perfis:
 *  - ADMIN: acesso total (cadastros da clínica e exclusões).
 *  - RECEPCAO: consulta tudo, cadastra/edita pacientes e agenda/cancela consultas,
 *              mas não exclui nada nem mexe em médicos, especialidades e convênios.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Exclusões: somente ADMIN
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                // Cadastros estruturais da clínica: somente ADMIN
                .requestMatchers(HttpMethod.POST, "/api/especialidades/**", "/api/convenios/**", "/api/medicos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/especialidades/**", "/api/convenios/**", "/api/medicos/**").hasRole("ADMIN")
                // Pacientes e consultas: ADMIN e RECEPCAO
                .requestMatchers("/api/pacientes/**", "/api/consultas/**").hasAnyRole("ADMIN", "RECEPCAO")
                // Leituras: qualquer usuário autenticado
                .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                .anyRequest().authenticated())
            .httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService usuarios(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode("admin123")).roles("ADMIN").build(),
                User.withUsername("recepcao").password(encoder.encode("recepcao123")).roles("RECEPCAO").build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
