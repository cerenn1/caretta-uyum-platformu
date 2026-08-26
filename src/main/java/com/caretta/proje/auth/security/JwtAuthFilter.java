package com.caretta.proje.auth.security;

import com.caretta.proje.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractUsername(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userRepository.findByEmail(email).orElse(null);

                // NOT: userDetails HER ISTEKTE veritabanindan TAZE cekiliyor (JWT payload'undan
                // degil), bu yuzden asagidaki isEnabled() kontrolu guncel (istek anindaki) aktif/pasif
                // durumunu yansitir. jwtService.isTokenValid(...) SADECE username+expiry kontrol eder,
                // aktif/pasif durumuna HIC bakmaz - yani token'in kendisi hala "gecerli" gorunur.
                // Pasif yapilan bir calisanin ELINDEKI token'i suresi dolmamis olsa bile artik ise
                // yaramamali, aksi halde isten ayrilan/devre disi birakilan bir calisanin erisimi
                // 7 gun boyunca (token suresi) acik kalirdi. Bu yuzden isEnabled() false ise
                // SecurityContext'e authentication SET ETMIYORUZ - istek kimliksiz devam eder,
                // korumali endpoint 401/403 doner (token gecerli gorunse bile).
                if (userDetails != null && jwtService.isTokenValid(token, userDetails) && userDetails.isEnabled()) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (JwtException ignored) {
            // gecersiz/suresi dolmus token -> kimliksiz devam eder, korumali endpoint 401 doner
        }

        filterChain.doFilter(request, response);
    }
}
