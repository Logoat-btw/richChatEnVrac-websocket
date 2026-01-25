*
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;
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
import richChatServer.model.MemberRepository;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.projections.RoomBasic;
import richChatServer.model.views.MemberViews;
import richChatServer.model.views.RoomViews;
import richChatServer.security.authentication.RichChatMemberDetails;
import richChatServer.services.RoomService;
import richChatServer.controllers.websocket.RoomTopicNotificationController;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/v1/rest/rooms")
public class RoomRestController {

    private final RoomService roomSvc;

    private final MemberRepository memRepo;

    private final Optional<RoomTopicNotificationController> roomNotifSvc;

    @Autowired
    public RoomRestController(RoomService roomSvc, MemberRepository memRepo,
            Optional<RoomTopicNotificationController> roomNotifSvc) {
        this.roomSvc = roomSvc;
        this.memRepo = memRepo;
        this.roomNotifSvc = roomNotifSvc;
    }

    @JsonView(NormalRoomAndUser.class)
    @PostMapping
    public Room createRoom(@RequestBody RoomCreation roomToCreate) {
        return this.roomSvc.createRoom(roomToCreate.name, roomToCreate.color);
    }

    @JsonView(NormalRoomAndUser.class)
    @GetMapping
    public List<RoomBasic> getUserRooms(@AuthenticationPrincipal RichChatMemberDetails currentUser) {
        final Member currentMember = currentUser.getMember();
        return this.roomSvc.getMemberRooms(currentMember);
    }

    @JsonView(DetailledRoomAndNormalUser.class)
    @GetMapping("{roomId:[abcdef0-9]{24}}")
    public Room getRoomDetails(@PathVariable String roomId, @AuthenticationPrincipal RichChatMemberDetails currentUser) {
        final Member currentMember = currentUser.getMember();
        return this.roomSvc.accessRoom(roomId, currentMember);
    }

    @DeleteMapping("{roomId:[abcdef0-9]{24}}")
    public ResponseEntity deleteRoom(@PathVariable String roomId) {
        // First retrieve the room by its id (will control authorization and existence)
        final Room room = this.roomSvc.accessRoom(roomId, null);
        this.roomSvc.deleteRoom(room);
        // If available, notify
        this.roomNotifSvc.ifPresent(rns -> rns.informRoomDeleted(room));
        return ResponseEntity.noContent().build();
    }

    @JsonView(DetailledRoomAndNormalUser.class)
    @PostMapping("{roomId:[abcdef0-9]{24}}/guests")
    public RoomGuest addGuest(@PathVariable String roomId, @RequestBody GuestCreation guestToCreate) {
        // First retrieve the room by its id (will control authorization and existence)
        final Room room = this.roomSvc.accessRoom(roomId, null);
        // Retrieve the user by its mail
        final Member member = this.memRepo.findByEmail(guestToCreate.email).orElseThrow(() -> new NoSuchElementException("User not found"));
        final RoomGuest guest = this.roomSvc.inviteGuest(room, member);
        // If available, notify
        this.roomNotifSvc.ifPresent(rns -> rns.informGuestAdded(room, guest));
        return guest;
    }

    @DeleteMapping("{roomId:[abcdef0-9]{24}}/guests/{guestId:[abcdef0-9]{24}}")
    public ResponseEntity removeGuest(@PathVariable String roomId, @PathVariable String guestId) {
        // First retrieve the room by its id (will control authorization and existence)
        final Room room = this.roomSvc.accessRoom(roomId, null);
        // Retrieve the user by its id
        final Member member = this.memRepo.findById(guestId).orElseThrow(() -> new NoSuchElementException("User not found"));
        this.roomSvc.removeGuest(room, member);
        // If available, notify
        this.roomNotifSvc.ifPresent(rns -> rns.informGuestRemoved(room, member));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("recent-activity")
    public Map<String, Boolean> hasRecentActivity(
            @RequestParam(name = "last", required = false, defaultValue = "") String lastLdt) {
        try {
            LocalDateTime ldt = lastLdt.isEmpty()
                    ? LocalDateTime.now().minusHours(1)
                    : LocalDateTime.parse(lastLdt, DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            Random rnd = new Random();
            int rndValue = rnd.nextInt(10);
            if (ldt.plusSeconds(5).isAfter(now)) {
                return Map.of("newMessages", rndValue > 7);
            } else {
                return Map.of("newMessages", rndValue > 2);
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Bad datetime format");
        }
    }

    public static record GuestCreation(String email) {

    }

    public static record RoomCreation(String name, String color) {

    }

    public interface NormalRoomAndUser extends RoomViews.Normal, MemberViews.Normal {

    }

    public interface DetailledRoomAndNormalUser extends RoomViews.Detailled, MemberViews.Normal {

    }

}
