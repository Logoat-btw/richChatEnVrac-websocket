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
package richChatServer.controllers.websocket;

import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.RoomMessage;

/**
 *
 * @author Rémi Venant
 */
public interface RoomTopicNotificationController {

    /**
     * Notify the member they has been added as a guest to the room.
     *
     * @param room
     * @param guest
     */
    void informGuestAdded(Room room, RoomGuest guest);

    /**
     * Notify all connected members of the room that member is not a guest
     * anymore.
     *
     * @param room
     * @param member
     */
    void informGuestRemoved(Room room, Member member);

    /**
     * Notify all connected members of the room that it has been deleted.
     *
     * @param room
     */
    void informRoomDeleted(Room room);

    /**
     * Notify all connected members of the room that a message has been added.
     *
     * @param room
     * @param message
     */
    void informMessageAdded(Room room, RoomMessage message);

    /**
     * Notify all connected members of the room that a message has been removed.
     *
     * @param room
     * @param messageId
     */
    void informMessageRemoved(Room room, String messageId);
}
