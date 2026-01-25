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

import java.security.Principal;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import richChatServer.model.websocket.RoomAddMessagerOrder;
import richChatServer.model.websocket.RoomMessageAddedOrder;
import richChatServer.model.websocket.RoomMessageRemovedOrder;
import richChatServer.model.websocket.RoomOrder;
import richChatServer.model.websocket.RoomRemoveMessageOrder;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomMessage;
import richChatServer.security.authentication.RichChatMemberDetails;
import richChatServer.services.RoomMessageService;
import richChatServer.services.RoomService;

/**
 *
 * @author Rémi Venant
 */
@Controller
@MessageMapping("rooms")
public class RoomWebsocketController {

    private final RoomService roomSvc;

    private final RoomMessageService roomMsgSvc;

    @Autowired
    public RoomWebsocketController(RoomService roomSvc, RoomMessageService roomMsgSvc) {
        this.roomSvc = roomSvc;
        this.roomMsgSvc = roomMsgSvc;
    }

    @MessageMapping("{roomId:[abcdef0-9]{24}}")
    public RoomOrder handleRoomOrder(
            @DestinationVariable("roomId") String roomId,
            @Payload RoomOrder order, Principal currentUser) {
        final RichChatMemberDetails author = RichChatMemberDetails.getFromPrincipal(currentUser);
        if (author == null) {
            throw new AccessDeniedException("No Member. Proper Authentication required");
        }

        // Set / override roomId and orderDateTime
        order.setRoomId(roomId);
        order.setOrderTime(Instant.now());

        // Handle order from its type
        return switch (order) {
            case RoomAddMessagerOrder addMsgOrder ->
                this.handleAddMessage(addMsgOrder);
            case RoomRemoveMessageOrder removeMsgOrder ->
                this.handleRemoveMessage(removeMsgOrder, author.getMember());
            default ->
                throw new IllegalArgumentException("Wrong order type");
        };
    }

    private RoomMessageAddedOrder handleAddMessage(RoomAddMessagerOrder order) {
        final Room room = this.roomSvc.accessRoom(order.getRoomId(), null);
        final RoomMessage message = this.roomMsgSvc.addMessageToRoom(room, order.getMessage());
        return RoomMessageAddedOrder.build(order, message);
    }

    private RoomMessageRemovedOrder handleRemoveMessage(RoomRemoveMessageOrder order, Member author) {
        final Room room = this.roomSvc.accessRoom(order.getRoomId(), null);
        if (order.isOwner()) {
            this.roomMsgSvc.deleteRoomMessage(room, order.getMessageId());
        } else {
            this.roomMsgSvc.deleteRoomMessageOfAuthor(room, author, order.getMessageId());
        }
        return RoomMessageRemovedOrder.build(order, order.getMessageId());
    }
}
