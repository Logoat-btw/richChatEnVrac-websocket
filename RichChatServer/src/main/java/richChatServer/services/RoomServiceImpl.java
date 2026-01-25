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
package richChatServer.services;

import jakarta.validation.ConstraintViolationException;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.RoomMessage;
import richChatServer.model.RoomMessageRepository;
import richChatServer.model.RoomRepository;
import richChatServer.model.projections.RoomBasic;
import richChatServer.model.projections.RoomMessageBasic;

/**
 *
 * @author Rémi Venant
 */
@Service
@Validated
public class RoomServiceImpl implements RoomService, RoomMessageService {

    private static final Log LOG = LogFactory.getLog(RoomServiceImpl.class);

    private final RoomRepository roomRepo;

    private final RoomMessageRepository roomMsgRepo;

    public RoomServiceImpl(RoomRepository roomRepo, RoomMessageRepository roomMsgRepo) {
        this.roomRepo = roomRepo;
        this.roomMsgRepo = roomMsgRepo;
    }

    @Override
    public List<RoomBasic> getMemberRooms(Member member) throws AccessDeniedException, ConstraintViolationException {
        // Collect rooms of member as a owner then as a guest
        Collection<RoomBasic> ownedRooms = this.roomRepo.findBasicByOwnerOrderByName(member);
        // Collect rooms of member as a guest and sort them
        Collection<RoomBasic> invitedRooms = this.roomRepo.findBasicByGuestsMemberOrderByName(member);
        // Return both collection concatenated
        return Stream.concat(ownedRooms.stream(), invitedRooms.stream()).toList();
    }

    @Override
    public Room accessRoom(String roomId, Member accessor) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException {
        // Retrieve the room; throw exception if not found
        Room room = this.roomRepo.findById(roomId).orElseThrow(() -> new NoSuchElementException("Room not found"));
        // if the accessor is given, check if the state is pending, update it otherwise
        if (accessor != null) {
            Optional<RoomGuest> guest = room.getGuests().stream().filter(g -> g.getMember().equals(accessor)).findAny();
            if (guest.isPresent() && Boolean.TRUE.equals(guest.get().getPending())) {
                guest.get().setPending(Boolean.FALSE);
                // Avoid saving the full room and just save the pending state for the guest
                long res = this.roomRepo.findAndSetGuestPendingByIdAndGuestMember(roomId, accessor, Boolean.FALSE);
                LOG.debug("REsult of set pending: " + res);
//                room = this.roomRepo.save(room);
            }
        }
        // return the room
        return room;
    }

    @Override
    public Room createRoom(String name, String color) throws AccessDeniedException, ConstraintViolationException {
        return this.roomRepo.save(new Room(name, color));
    }

    @Override
    public void deleteRoom(Room room) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException {
        // Remove the room
        this.roomRepo.delete(room);
        // Remove all related messages
        this.roomMsgRepo.deleteByRoom(room);

    }

    @Override
    public RoomGuest inviteGuest(Room room, Member member) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException, DuplicateKeyException {
        // Check the member is not the owner
        if (room.getOwner().equals(member)) {
            throw new DuplicateKeyException("Member is already the owner and cannot be added as a guest.");
        }
        // Check the member is not already a guest, whatever his state is
        if (room.getGuests().stream().anyMatch(g -> g.getMember().equals(member))) {
            throw new DuplicateKeyException("Member is already a guest.");
        }
        // Inject directly the room guest to avoid saving the complete room
        RoomGuest guest = new RoomGuest(member, Boolean.TRUE);
        long res = this.roomRepo.findAndPushGuestById(room.getId(), guest);
        if (res == 0) {
            throw new DuplicateKeyException("Member is already a guest (should not happen here!).");
        }
        // A the guest to the local representation for consistency
        room.getGuests().add(guest);
        return guest;
    }

    @Override
    public void removeGuest(Room room, Member member) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException {
        // Remove directly the room guest to avoid saving the complete room
        long res = this.roomRepo.findAndPullGuestById(room.getId(), member);
        if (res == 0) {
            throw new NoSuchElementException("Member is not a guest.");
        }
        // Remove the guest from local representation for consistency
        room.getGuests().remove(new RoomGuest(member, null));
    }

    @Override
    public List<RoomMessageBasic> getRoomMessages(Room room) throws AccessDeniedException, ConstraintViolationException {
        return this.roomMsgRepo.streamByRoomOrderByCreationAsc(room).toList();
    }

    @Override
    public RoomMessage addMessageToRoom(Room room, String message) throws AccessDeniedException, ConstraintViolationException {
        return this.roomMsgRepo.save(new RoomMessage(room, message));
    }

    @Override
    public void deleteRoomMessage(Room room, String messageId) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException {
        long res = this.roomMsgRepo.deleteByRoomAndId(room, messageId);
        if (res == 0) {
            throw new NoSuchElementException("Message not found in room.");
        }
    }

    @Override
    public void deleteRoomMessageOfAuthor(Room room, Member author, String messageId) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException {
        long res = this.roomMsgRepo.deleteByRoomAndIdAndAuthor(room, messageId, author);
        if (res == 0) {
            throw new NoSuchElementException("Message not found in room for author.");
        }
    }

}
