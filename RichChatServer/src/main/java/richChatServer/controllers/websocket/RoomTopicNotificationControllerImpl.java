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

import java.time.Instant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.RoomMessage;
import richChatServer.model.websocket.NewRoomForGuestOrder;
import richChatServer.model.websocket.RoomDeletedOrder;
import richChatServer.model.websocket.RoomGuestAddedOrder;
import richChatServer.model.websocket.RoomGuestRemovedOrder;
import richChatServer.model.websocket.RoomMessageAddedOrder;
import richChatServer.model.websocket.RoomMessageRemovedOrder;

/**
 *
 * @author Rémi Venant
 */
@ConditionalOnBean(SimpMessagingTemplate.class)
@Service
public class RoomTopicNotificationControllerImpl implements RoomTopicNotificationController {

    private static final Log LOG = LogFactory.getLog(RoomTopicNotificationControllerImpl.class);

    private final SimpMessagingTemplate msgTemplate;

    public RoomTopicNotificationControllerImpl(SimpMessagingTemplate msgTemplate) {
        this.msgTemplate = msgTemplate;
    }

    @Async
    @Override
    public void informGuestAdded(Room room, RoomGuest guest) {
        RoomGuestAddedOrder order = new RoomGuestAddedOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(room.getId());
        order.setMember(guest.getMember());
        order.setPending(guest.getPending());
        // Notify all users connected to the room topic
        this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(room), order);
        // Notify the new guest on his queue 
        NewRoomForGuestOrder guestOrder = new NewRoomForGuestOrder();
        guestOrder.setOrderTime(Instant.now());
        guestOrder.setRoomId(room.getId());
        guestOrder.setName(room.getName());
        guestOrder.setColor(room.getColor());
        guestOrder.setOwner(room.getOwner());
        this.msgTemplate.convertAndSendToUser(guest.getMember().getId(), "/queue/rooms", guestOrder);
    }

    @Async
    @Override
    public void informGuestRemoved(Room room, Member member) {
        RoomGuestRemovedOrder order = new RoomGuestRemovedOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(room.getId());
        order.setMember(member);
        // Notify all users connected to the room topic
        this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(room), order);
        // Notify the new guest on his queue 
        this.msgTemplate.convertAndSendToUser(member.getId(), "/queue/rooms", order);
    }

    @Async
    @Override
    public void informRoomDeleted(Room room) {
        RoomDeletedOrder order = new RoomDeletedOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(room.getId());
        // Notify all users on their private channel
        room.getGuests().forEach((guest) -> {
            this.msgTemplate.convertAndSendToUser(guest.getMember().getId(), "/queue/rooms", order);
        });
        //this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(room), order);
    }

    @Async
    @Override
    public void informMessageAdded(Room room, RoomMessage message) {
        RoomMessageAddedOrder order = new RoomMessageAddedOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(room.getId());
        order.setMessage(message);
        this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(room), order);
    }

    @Async
    @Override
    public void informMessageRemoved(Room room, String messageId) {
        RoomMessageRemovedOrder order = new RoomMessageRemovedOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(room.getId());
        order.setMessageId(messageId);
        this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(room), order);
    }
}
