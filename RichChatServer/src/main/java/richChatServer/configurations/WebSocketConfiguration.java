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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.messaging.Message;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.StompWebSocketEndpointRegistration;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import richChatServer.controllers.websocket.RoomTopicSubscriptionWatcher;
import richChatServer.model.RoomRepository;
import richChatServer.security.authorizations.RoomTopicSubscriptionAuthorizationManager;

/**
 *
 * @author Rémi Venant
 */
@Configuration
@EnableWebSocketMessageBroker
@EnableAsync
@EnableWebSocketSecurity
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private static final Log LOG = LogFactory.getLog(WebSocketConfiguration.class);

    private final Optional<CorsConfiguration> corsConfiguration;

    @Autowired
    public WebSocketConfiguration(Optional<CorsConfiguration> corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        StompWebSocketEndpointRegistration reg = registry.addEndpoint("/api/v1/websocket");
        this.configureAllowedOriginForRegistration(reg);
        reg.withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue"); //Topic will be use to broadcast message, queue to unicast
        registry.setApplicationDestinationPrefixes("/app");
    }

    private StompWebSocketEndpointRegistration configureAllowedOriginForRegistration(StompWebSocketEndpointRegistration reg) {
        if (this.corsConfiguration.isPresent()) {
            LOG.warn("CONFIGURE WEBSOCKET CORS FROM CONFIGURATION");
            final CorsConfiguration corsConfig = this.corsConfiguration.get();
            List<String> origins = corsConfig.getAllowedOrigins();
            if (origins != null) {
               String [] allowedOrigins = new String[origins.size()];
               allowedOrigins = origins.toArray(allowedOrigins);
                reg.setAllowedOrigins(allowedOrigins);
            }
            origins = corsConfig.getAllowedOriginPatterns();
            if (origins != null) {
               String [] allowedOriginPatterns = new String[origins.size()];
               allowedOriginPatterns = origins.toArray(allowedOriginPatterns);
                reg.setAllowedOriginPatterns(allowedOriginPatterns);
            }
        }
        return reg;
    }

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public RoomTopicSubscriptionWatcher roomTopicSubscriptionWatcher(SimpMessagingTemplate msgTemplate) {
        return new RoomTopicSubscriptionWatcher(msgTemplate, "/topic/rooms/");
    }

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager(
        MessageMatcherDelegatingAuthorizationManager.Builder messages,
        RoomRepository roomRepository
    ) {
        messages.nullDestMatcher().authenticated()
        .simpDestMatchers("/app/rooms/*").authenticated()
        .simpSubscribeDestMatchers("/user/queue/errors", "/user/queue/rooms").authenticated()
        .simpSubscribeDestMatchers("/topic/rooms/*")
        .access(new RoomTopicSubscriptionAuthorizationManager(roomRepository, "/topic/rooms/"))
        .anyMessage().denyAll();
        return messages.build();
    }
}
