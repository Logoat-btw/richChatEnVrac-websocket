/*
 * Copyright (C) 2025 IUT Laval - Le Mans Université
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package richChatServer.configurations;

import jakarta.validation.Validator;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import richChatServer.security.authentication.RestAuthenticationFilter;
import richChatServer.security.authentication.RichChatMemberDetailsService;
import richChatServer.security.csrf.SpaCsrfTokenRequestHandler;

/**
 *
 * @author Rémi Venant
 */
@Configuration
@EnableWebSecurity
public class SecurityWebConfiguration {

    private static final Log LOG = LogFactory.getLog(SecurityWebConfiguration.class);

    @Value("${server.servlet.session.cookie.name:JSESSIONID}")
    private String sessionCookieName;

    @Autowired
    private AppSecurityProperties appSecurityProperties;

    /**
     * Service de récupération des credentials utilisateur depuis la BD Mongo
     *
     * @param mongoTemplate
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService(MongoTemplate mongoTemplate) {
        return new RichChatMemberDetailsService(mongoTemplate, this.appSecurityProperties.getLocalAdmin());
    }

    /**
     * Encodeur de mot de passe trouvé sur le net
     *
     * @return
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword != null && encodedPassword != null
                        && rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    /**
     * Fournisseur d'identité de base s'appuyant sur le user details service et
     * l'encodeur en vigueur
     *
     * @param userDetailsService
     * @param encoder
     * @return
     */
    @Bean
    public DaoAuthenticationProvider mongoLocalAuthenticatitionProvider(UserDetailsService userDetailsService, PasswordEncoder encoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(encoder);
        return authProvider;
    }

    /**
     * Gestion de l'authentification fondé sur les authentification providers
     * existant
     *
     * @param authenticationProviders
     * @return
     */
    @Bean
    public AuthenticationManager authenticationManager(List<AuthenticationProvider> authenticationProviders) {
        return new ProviderManager(authenticationProviders);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
    }

    @Bean
    public RestAuthenticationFilter restAuthenticationFilter(
            AuthenticationManager authenticationManager,
            SecurityContextRepository securityContextRepository,
            Validator validator) throws Exception {
        RestAuthenticationFilter filter = new RestAuthenticationFilter(validator);
        filter.setFilterProcessesUrl("/api/v1/rest/login");
        filter.setAuthenticationManager(authenticationManager);
        filter.setAuthenticationSuccessHandler(new SimpleUrlAuthenticationSuccessHandler("/api/v1/rest/accounts/myself"));
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
        filter.setSecurityContextRepository(securityContextRepository);
        return filter;
    }

    @Bean
    @ConditionalOnBooleanProperty(name = "app.security.cors-dev", havingValue = true)
    CorsConfiguration corsConfiguration() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:*", "http://127.0.0.1:*", "http://*:*", "moz-extension://*"));
        configuration.setAllowedMethods(Arrays.asList("OPTIONS", "HEAD", "GET", "POST", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("content-type", "Accept", "Accept-Language", "Authorization", "X-Requested-With", "x-xsrf-token", SpaCsrfTokenRequestHandler.CSRF_DEV_HEADER_NAME));
        //configuration.setExposedHeaders(List.of(SpaCsrfTokenRequestHandler.CSRF_DEV_HEADER_NAME));
        configuration.setAllowCredentials(Boolean.TRUE);
        configuration.setMaxAge(Duration.ofHours(6));
        return configuration;
    }

    @Bean
    @ConditionalOnBooleanProperty(name = "app.security.cors-dev", havingValue = true)
    CorsConfigurationSource corsConfigurationSource(CorsConfiguration corsConfiguration) {
        LOG.warn("CREATE CORS-DEV configuration");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }

    // Security on rest enpoints and authentication/session mgmt
    @Bean
    public SecurityFilterChain MultiSecFilterChain(HttpSecurity http,
            RestAuthenticationFilter restAuthenticationFilter,
            SecurityContextRepository securityContextRepository,
            Optional<CorsConfigurationSource> corsConfigurationSource) throws Exception {
        // Gestion de l'accès non autorisé : renvoie simplement une 403, du point d'autentification,
        // de la déconnection
        http
                .exceptionHandling(eh -> eh.accessDeniedHandler(new AccessDeniedHandlerImpl()))
                .addFilterAt(restAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logoutCustomizer -> logoutCustomizer.logoutUrl("/api/v1/rest/logout")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies(this.sessionCookieName)
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
                );
        // Gestion des session : création uniquement si demandé, au max 5 par utilisateur, et force la création
        // de nouvelle session
        http.sessionManagement(sm -> {
            sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED).maximumSessions(5);
            sm.sessionFixation(sf -> sf.newSession());
        });
        // Gestion du stockage des session
        http.securityContext(scc -> {
            scc.securityContextRepository(securityContextRepository);
        });

        // Protection pour pour SockJS en cas d'injection de Frame dans la page sur vieux navigateurs
        http.headers(hc -> {
            hc.frameOptions(foc -> foc.sameOrigin());
        });
        // Gestion du CORS au besoin
        http.cors(cors -> {
            if (this.appSecurityProperties.isCorsDev()) {
                LOG.info("USE CORS-DEV MANAGEMENT FOR REST SECURITY");
                cors.configurationSource(corsConfigurationSource.get());
            } else {
                LOG.info("DISABLE CORS FOR HTTP");
                cors.disable();
            }
        });

        http.csrf(csrf -> {
            // SIGNED DOUBLE SUBMIT COOKIE
            if (this.appSecurityProperties.isCsrf()) {
                LOG.info("ENFORCE CSRF Token management");
                CookieCsrfTokenRepository csrfTokenRepo = new CookieCsrfTokenRepository();
                csrfTokenRepo.setCookieName("XSRF-TOKEN");
                csrfTokenRepo.setHeaderName("X-XSRF-TOKEN");
                csrfTokenRepo.setCookieCustomizer(cookieCustomizer -> {
                    cookieCustomizer.path("/").httpOnly(false).sameSite("Lax").build();
                });
                final CsrfTokenRequestHandler csrfHandler = new SpaCsrfTokenRequestHandler();
                csrf.csrfTokenRepository(csrfTokenRepo).csrfTokenRequestHandler(csrfHandler);
            } else {
                LOG.info("DISABLE CSRF");
                csrf.disable();
            }
        });

        // Par-feu applicatif
        http.authorizeHttpRequests(ahr -> {
            ahr
                    .requestMatchers(HttpMethod.OPTIONS).permitAll() // requête OPTIONS pour cors
                    .requestMatchers("/api/**").permitAll()// toute l'api
                    .requestMatchers(HttpMethod.GET).permitAll() // Autres ressources (static)
                    .anyRequest().denyAll(); // Tout autre requete 
        });

        return http.build();
    }
}
