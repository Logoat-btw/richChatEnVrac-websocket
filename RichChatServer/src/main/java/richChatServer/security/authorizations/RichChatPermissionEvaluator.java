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

import java.io.Serializable;
import java.util.Objects;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import richChatServer.model.Room;

/**
 *
 * @author Rémi Venant
 */
public class RichChatPermissionEvaluator implements PermissionEvaluator {

    private static final Log LOG = LogFactory.getLog(RichChatPermissionEvaluator.class);

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        final String userId = authentication.getName();
        final String perm = Objects.toString(permission).toLowerCase();
        // No rules defined to handle null targetDomainObject
        if (targetDomainObject == null) {
            LOG.warn("Permission evaluated on null targetDomainObject: " + perm);
            return false;
        }
        if (targetDomainObject instanceof Room room) {
            LOG.debug("Evaluating permission on room");
            return this.handleRoomPermission(userId, room, perm);
        } else {
            LOG.warn("Permission \"" + perm + "\" evaluated on targetDomainObject type: " + targetDomainObject.getClass().getName());
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        LOG.warn("Complex permission not handled in RichChatPermissionEvaluator");
        return false;
    }

    private boolean handleRoomPermission(String userId, Room room, String permission) {
        switch (permission) {
            case "owns" -> {
                return room.getOwner().getId().equals(userId);
            }
            case "access" -> {
                return room.getOwner().getId().equals(userId)
                        || room.getGuests().stream().anyMatch(g -> g.getMember().getId().equals(userId));
            }
            default -> {
                LOG.warn("Invalid room permission: " + permission);
                return false;
            }
        }
    }

}
