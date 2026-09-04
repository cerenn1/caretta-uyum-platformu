package com.caretta.proje.auth.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/oteller").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/oteller").authenticated()
                        // Kisisel veri icermeyen, tamamen agregat (toplu sayi) istatistikler -
                        // fon basvurusu/ortaklik gorusmelerinde disariya (Greenpeace, WWF vb.)
                        // gosterilebilmesi icin bilerek herkese acik birakildi.
                        .requestMatchers(HttpMethod.GET, "/api/istatistikler").permitAll()
                        // Ayni gerekceyle (kisisel veri yok, "Etkimiz / Kapsam Alanimiz"
                        // bolumunde disariya/fon kuruluslarina gosterilecek) bilerek permitAll.
                        .requestMatchers(HttpMethod.GET, "/api/kapsam-alani").permitAll()
                        // Stripe webhook'u JWT/kullanici girisi GEREKTIRMEZ - kendi guvenligini
                        // Stripe-Signature basligindaki imza saglar (bkz. StripeOdemeServisi#webhookIsle).
                        .requestMatchers(HttpMethod.POST, "/api/stripe/webhook").permitAll()
                        // Demo amacli manuel premium isaretleme araci - JWT GEREKTIRMEZ, kendi
                        // guvenligini paylasilan X-Admin-Key basligi saglar (bkz. UyelikService).
                        .requestMatchers(HttpMethod.POST, "/api/admin/otel/*/premium-durum").permitAll()
                        // Ayni demo-arac gerekcesi (bkz. UyelikService): JWT GEREKTIRMEZ, kendi
                        // guvenligini X-Admin-Key basligi saglar.
                        .requestMatchers(HttpMethod.POST, "/api/admin/otel/*/koltuk-sayisi").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/otel/*/kapanis-kaniti-doldur").permitAll()
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
