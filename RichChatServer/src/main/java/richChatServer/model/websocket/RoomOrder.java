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
package richChatServer.model.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import java.time.Instant;

/**
 *
 * @author Rémi Venant
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "orderType", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = RoomGuestAddedOrder.class, name = "guestAdded"),
    @JsonSubTypes.Type(value = NewRoomForGuestOrder.class, name = "newRoom"),
    @JsonSubTypes.Type(value = RoomGuestRemovedOrder.class, name = "guestRemoved"),
    @JsonSubTypes.Type(value = RoomGuestConnectedOrder.class, name = "guestConnected"),
    @JsonSubTypes.Type(value = RoomGuestDisconnectedOrder.class, name = "guestDisconnected"),
    @JsonSubTypes.Type(value = RoomDeletedOrder.class, name = "roomDeleted"),
    @JsonSubTypes.Type(value = RoomAddMessagerOrder.class, name = "addMessage"),
    @JsonSubTypes.Type(value = RoomRemoveMessageOrder.class, name = "removeMessage"),
    @JsonSubTypes.Type(value = RoomMessageAddedOrder.class, name = "messageAdded"),
    @JsonSubTypes.Type(value = RoomMessageRemovedOrder.class, name = "messageRemoved"),
    @JsonSubTypes.Type(value = RoomConnectedGuestsOrder.class, name = "connectedGuests"),})
public class RoomOrder {

    @JsonView(OrderView.class)
    private String orderType;

    @JsonView(OrderView.class)
    private String roomId;

    @JsonView(OrderView.class)
    private Instant orderTime;

    public RoomOrder() {
    }

    public RoomOrder(String orderType, String roomId, Instant orderTime) {
        this.orderType = orderType;
        this.roomId = roomId;
        this.orderTime = orderTime;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Instant getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Instant orderTime) {
        this.orderTime = orderTime;
    }

}
