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
package richChatServer.security.authorizations;

import java.util.function.Supplier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.intercept.MessageAuthorizationContext;
import richChatServer.model.RoomRepository;

/**
 *
 * @author Rémi Venant
 * @param <T>
 */
public class RoomTopicSubscriptionAuthorizationManager<T> implements AuthorizationManager<MessageAuthorizationContext<T>> {

    private static final Log LOG = LogFactory.getLog(RoomTopicSubscriptionAuthorizationManager.class);

    private final AuthorizationManager authenticatedAuthorizationManager = AuthenticatedAuthorizationManager.authenticated();

    private final RoomRepository roomRepository;

    private final String topicPrefix;

    public RoomTopicSubscriptionAuthorizationManager(RoomRepository roomRepository, String topicPrefix) {
        this.roomRepository = roomRepository;
        this.topicPrefix = topicPrefix;
    }

    private String checkSubscriptionAndExtractRoomId(Message<T> message) {
        if (message == null) {
            LOG.debug("Authorizing composition topic sub: No message");
            return null;
        }
        // Extract Stomp headers
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);
        if (sha == null) {
            LOG.debug("Authorizing composition topic sub: No stom headers");
            return null;
        }
        // Check command is subscription
        if (sha.getCommand() != StompCommand.SUBSCRIBE) {
            LOG.debug("Authorizing composition topic sub: No subscription message");
            return null;
        }
        // Check a destination is given
        final String topicDestination = sha.getDestination();
        if (topicDestination == null) {
            LOG.debug("Authorizing composition topic sub: no destination topic");
            return null;
        }
        // Attempt to retrieve the roomId
        if (topicDestination.startsWith(this.topicPrefix)) {
            String roomId = topicDestination.substring(this.topicPrefix.length());
            if (!roomId.isBlank()) {
                return roomId;
            }
        }
        LOG.debug("Authorizing composition topic sub: bad destination topic");
        return null;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, MessageAuthorizationContext<T> object) {
        LOG.debug("User authorized to subscribe to room ? ");
        try {
            // Use Authentified ?
            AuthorizationResult isAuthentified = this.authenticatedAuthorizationManager.authorize(authentication, object);
            if (isAuthentified == null || !isAuthentified.isGranted()) {
                LOG.debug("Authorizing composition topic sub: user not authentified");
                return new AuthorizationDecision(false);
            }
            // Message is subscription with compositionId ?
            final String roomId = this.checkSubscriptionAndExtractRoomId(object.getMessage());
            if (roomId == null) {
                LOG.debug("Authorizing composition topic sub: no composition id");
                return new AuthorizationDecision(false);
            }
            // Extract current user. Authencation will not return null to get() call as user is already authentified
            final String userId = authentication.get().getName();

            // Use can access the composition collaboratively ?
            if (!this.roomRepository.existsByIdAndOwnerOrGuestsMember(roomId, userId)) {
                LOG.debug("Authorizing composition topic sub: user cannot access the compo collaboratively");
                return new AuthorizationDecision(false);
            }
            LOG.debug("User authorized to subscribe to room topic of id " + roomId);
            // User ha access
            return new AuthorizationDecision(true);
        } catch (Throwable ex) {
            LOG.warn("Unexpected error happend", ex);
            return new AuthorizationDecision(false);
        }
    }
}
