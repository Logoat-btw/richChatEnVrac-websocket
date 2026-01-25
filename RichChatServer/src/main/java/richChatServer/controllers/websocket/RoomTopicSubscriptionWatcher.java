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
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import richChatServer.model.websocket.RoomConnectedGuestsOrder;
import richChatServer.model.websocket.RoomGuestConnectedOrder;
import richChatServer.model.websocket.RoomGuestDisconnectedOrder;

/**
 * Watch A user's Room Topic Subscription and quitting (Unsub or disconnect) to
 * notify other users of the room and notify this particular user of currently
 * connected users on the room topic at subscription
 *
 * @author Rémi Venant
 */
public class RoomTopicSubscriptionWatcher {

    private static final Log LOG = LogFactory.getLog(RoomTopicSubscriptionWatcher.class);

    private final ConcurrentHashMap<String, ConcurrentSkipListSet<RoomIdAndSession>> roomIdsAndSessionsByUser = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate msgTemplate;

    private final String topicPrefix;

    public RoomTopicSubscriptionWatcher(SimpMessagingTemplate msgTemplate, String topicPrefix) {
        this.msgTemplate = msgTemplate;
        this.topicPrefix = topicPrefix;
    }

    @EventListener
    public void onSessionSubscribeEvent(SessionSubscribeEvent event) {
        // Extract member
        final Principal user = event.getUser();
        if (user == null || user.getName() == null) {
            LOG.warn("No current user or user id found on stomp subscription.");
            return;
        }
        final String memberId = user.getName();
        // Extract roomId from the topic destination of the subscription
        // Extract Stomp headers
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha == null) {
            LOG.debug("No stomp headers.");
            return;
        }
        final String sessionId = sha.getSessionId();
        // Check command is subscription
        if (sha.getCommand() != StompCommand.SUBSCRIBE) {
            LOG.debug("No subscription message.");
            return;
        }
        // Check a destination is given
        final String topicDestination = sha.getDestination();
        if (topicDestination == null) {
            LOG.debug("No destination topic.");
            return;
        }
        // Attempt to retrieve the roomId
        String roomId = null;
        if (topicDestination.startsWith(this.topicPrefix)) {
            roomId = topicDestination.substring(this.topicPrefix.length());
        }
        if (roomId == null || roomId.isBlank()) {
            LOG.trace("No room id found.");
            return;
        }
        LOG.debug("Session started from user " + memberId + " on room " + roomId);
        // Remember userId-{roomId, session}
        this.roomIdsAndSessionsByUser.computeIfAbsent(memberId, (k) -> new ConcurrentSkipListSet<>(new RoomIdAndSessionComparator()))
                .add(new RoomIdAndSession(roomId, sessionId));
        // notify others of the connected users in the room topic
        LOG.debug("Notify users of the room that " + memberId + " is connected");
        final RoomGuestConnectedOrder order = new RoomGuestConnectedOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(roomId);
        order.setMemberId(memberId);
        this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(roomId), order);
        // notify the connected user of other currently connected users in the room topic (async)
        this.notifyConnectedUsersToUserInRoom(memberId, roomId);
    }

    @EventListener
    public void onSessionUnsubscribe(SessionUnsubscribeEvent event) {
        // Extract member
        final Principal user = event.getUser();
        if (user == null || user.getName() == null) {
            LOG.warn("No current user or user id found on stomp subscription.");
            return;
        }
        final String memberId = user.getName();
        // Extract sessionId
        final StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        if (sha == null) {
            LOG.debug("No stomp headers.");
            return;
        }
        final String sessionId = sha.getSessionId();
        // Puul {roomId, session} from roomIdsAndSessionsByUser and remove the user list if emtpy after
        // Do the operation atomically and store the roomId in an atomic received
        final AtomicReference<String> roomIdCont = new AtomicReference<>();
        this.roomIdsAndSessionsByUser.computeIfPresent(memberId, (mid, riasList) -> {
            final Optional<RoomIdAndSession> userRias = riasList.stream()
                    .filter(rias -> rias.sessionId().equals(sessionId))
                    .findAny();
            if (userRias.isEmpty()) {
                LOG.warn("No matching user RIAS on session unsubscribe");
                return riasList;
            }
            roomIdCont.set(userRias.get().roomId);
            if (!riasList.remove(userRias.get())) {
                LOG.warn("User RIAS found be not removed???");
            }
            if (riasList.isEmpty()) {
                LOG.debug("User RIAS list empty, remove the list");
            }
            return riasList.isEmpty() ? null : riasList;
        });
        // notify others of the disconnected user in the room topic
        String roomId = roomIdCont.get();
        if (roomId != null) {
            LOG.debug("Notify other users of room that user is disconnected");
            RoomGuestDisconnectedOrder order = new RoomGuestDisconnectedOrder();
            order.setOrderTime(Instant.now());
            order.setRoomId(roomId);
            order.setMemberId(memberId);
            this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(roomId), order);
        }
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        // Extract member
        final Principal user = event.getUser();
        if (user == null || user.getName() == null) {
            LOG.warn("No current user or user id found on stomp subscription.");
            return;
        }
        final String memberId = user.getName();
        // Pull the list of {roomId, session}  of the user from roomIdsAndSessionsByUser (remove the user)
        ConcurrentSkipListSet<RoomIdAndSession> userRoomsAndSessions = this.roomIdsAndSessionsByUser.remove(memberId);
        if (userRoomsAndSessions != null) {
            // For each roomId, notify others of the disconnected user in the room topic
            userRoomsAndSessions.stream().map(RoomIdAndSession::roomId).forEach((roomId) -> {
                RoomGuestDisconnectedOrder order = new RoomGuestDisconnectedOrder();
                order.setOrderTime(Instant.now());
                order.setRoomId(roomId);
                order.setMemberId(memberId);
                this.msgTemplate.convertAndSend(RoomTopicUtils.getRoomTopicDestination(roomId), order);
            });
        }
    }

    @Async
    private void notifyConnectedUsersToUserInRoom(String targetUsername, String roomId) {
        // Build a list of connected userIds for the given room
        final List<String> userIds = this.roomIdsAndSessionsByUser.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(rias -> rias.roomId().equals(roomId)))
                .map(Entry::getKey)
                .toList();
        // Create and send order to the user
        RoomConnectedGuestsOrder order = new RoomConnectedGuestsOrder();
        order.setOrderTime(Instant.now());
        order.setRoomId(roomId);
        order.setMemberIds(userIds);
        LOG.debug("Send connected users to " + targetUsername);
        this.msgTemplate.convertAndSendToUser(targetUsername, "/queue/rooms", order);
    }

    private static record RoomIdAndSession(String roomId, String sessionId) {

    }

    private static class RoomIdAndSessionComparator implements Comparator<RoomIdAndSession> {

        @Override
        public int compare(RoomIdAndSession o1, RoomIdAndSession o2) {
            if (o1 == null) {
                return o2 == null ? 0 : 1;
            } else if (o2 == null) {
                return -1;
            } else {
                int res = o1.roomId.compareTo(o2.roomId);
                if (res == 0) {
                    res = o1.sessionId.compareTo(o2.sessionId);
                }
                return res;
            }
        }

    }
}
