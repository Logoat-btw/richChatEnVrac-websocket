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
package richChatServer.controllers.rest;

import com.fasterxml.jackson.annotation.JsonView;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomMessage;
import richChatServer.model.projections.RoomMessageBasic;
import richChatServer.model.views.MemberViews;
import richChatServer.model.views.RoomMessageViews;
import richChatServer.security.authentication.RichChatMemberDetails;
import richChatServer.services.RoomMessageService;
import richChatServer.services.RoomService;
import richChatServer.controllers.websocket.RoomTopicNotificationController;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/v1/rest/rooms/{roomId:[abcdef0-9]{24}}/messages")
public class RoomMessageController {

    private final RoomService roomSvc;

    private final RoomMessageService roomMsgSvc;

    private final RoomTopicNotificationController notificationController;

    @Autowired
    public RoomMessageController(RoomService roomSvc, RoomMessageService roomMsgSvc, RoomTopicNotificationController notificationController) {
        this.roomSvc = roomSvc;
        this.roomMsgSvc = roomMsgSvc;
        this.notificationController = notificationController;
    }

    @JsonView(RoomMessageViews.Normal.class)
    @GetMapping
    public List<RoomMessageBasic> getRoomMessages(@PathVariable String roomId) {
        // First retrieve the room by its id (will control authorization and existence)
        final Room room = this.roomSvc.accessRoom(roomId, null);
        return this.roomMsgSvc.getRoomMessages(room);
    }

    @JsonView(DetailledRoomMessageAndNormalUser.class)
    @PostMapping
    public RoomMessage createRoomMessage(@PathVariable String roomId, @RequestBody MessageCreation msgToCreate) {
        // First retrieve the room by its id (will control authorization and existence)
        final Room room = this.roomSvc.accessRoom(roomId, null);
        final RoomMessage message = this.roomMsgSvc.addMessageToRoom(room, msgToCreate.message);
        this.notificationController.informMessageAdded(room, message);
        return message;
    }

    @DeleteMapping("{msgId:[abcdef0-9]{24}}")
    public ResponseEntity removeRoomMessage(
            @PathVariable String roomId, @PathVariable String msgId,
            @RequestParam(name = "owner", required = false, defaultValue = "") String asOwner,
            @AuthenticationPrincipal RichChatMemberDetails currentUser) {
        // First retrieve the room by its id (will control authorization and existence)
        final Room room = this.roomSvc.accessRoom(roomId, null);
        // check if the owner signal is given
        final boolean deleteFromOwner = asOwner != null && !asOwner.isBlank() && !asOwner.equalsIgnoreCase("no");

        if (deleteFromOwner) {
            // Delete the message as the owner
            this.roomMsgSvc.deleteRoomMessage(room, msgId);
        } else {
            // Delete the message as the author
            final Member currentMember = currentUser.getMember();
            this.roomMsgSvc.deleteRoomMessageOfAuthor(room, currentMember, msgId);
        }
        this.notificationController.informMessageRemoved(room, msgId);
        return ResponseEntity.noContent().build();
    }

    public static record MessageCreation(String message) {

    }

    public interface DetailledRoomMessageAndNormalUser extends RoomMessageViews.Detailled, MemberViews.Normal {

    }
}
