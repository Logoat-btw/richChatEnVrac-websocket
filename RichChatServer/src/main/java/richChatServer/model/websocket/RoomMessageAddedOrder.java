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

import com.fasterxml.jackson.annotation.JsonView;
import richChatServer.model.RoomMessage;

/**
 *
 * @author Rémi Venant
 */
public class RoomMessageAddedOrder extends RoomOrder {

    @JsonView(OrderView.class)
    private RoomMessage message;

    public RoomMessage getMessage() {
        return message;
    }

    public void setMessage(RoomMessage message) {
        this.message = message;
    }

    public static RoomMessageAddedOrder build(RoomAddMessagerOrder order, RoomMessage message) {
        RoomMessageAddedOrder orderOut = new RoomMessageAddedOrder();
        orderOut.setRoomId(order.getRoomId());
        orderOut.setOrderTime(order.getOrderTime());
        orderOut.setMessage(message);
        return orderOut;
    }
}
