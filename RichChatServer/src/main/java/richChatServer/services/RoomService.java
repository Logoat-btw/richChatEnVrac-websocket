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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.projections.RoomBasic;

/**
 *
 * @author Rémi Venant
 */
public interface RoomService {

    /**
     * Get all Rooms (Basic representation) for the given member. Rooms are
     * sorted by owned rooms first, then invited. For all sub list, rooms are
     * sorted by name
     *
     * @param member the member
     * @return the list of rooms
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     */
    @PreAuthorize("hasRole('USER')")
    List<RoomBasic> getMemberRooms(
            @NotNull Member member)
            throws AccessDeniedException, ConstraintViolationException;

    /**
     * Access a room by its id. If the accessor member is given and is a guest
     * in pending state, will set the pending state as false
     *
     * @param roomId
     * @param accessor optional, used to mark a guest as not pending if any
     * @return the room
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     * @throws NoSuchElementException if the room cannot be find
     */
    @PostAuthorize("hasRole('ADMIN') or hasPermission(returnObject, 'access')")
    Room accessRoom(
            @NotNull @Pattern(regexp = "[abcdef0-9]{24}", flags = Pattern.Flag.CASE_INSENSITIVE) String roomId,
            Member accessor)
            throws AccessDeniedException, ConstraintViolationException, NoSuchElementException;

    /**
     * Create a new room
     *
     * @param name
     * @param color
     * @return
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException
     */
    @PreAuthorize("hasRole('USER')")
    Room createRoom(
            @NotBlank String name, String color)
            throws AccessDeniedException, ConstraintViolationException;

    /**
     * Delete a room, with all its messages
     *
     * @param room the room
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     * @throws NoSuchElementException if the room cannot be find
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#room, 'owns')")
    void deleteRoom(
            @NotNull Room room)
            throws AccessDeniedException, ConstraintViolationException;

    /**
     * Add a new guest to a room, with a pending state to true
     *
     * @param room the room
     * @param member member to add as a guest in the room
     * @return the room guest representation
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     * @throws NoSuchElementException if the room cannot be find
     * @throws DuplicateKeyException if the member is already present in the
     * room as a guest or owner
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#room, 'owns')")
    RoomGuest inviteGuest(
            @NotNull Room room,
            @NotNull Member member)
            throws AccessDeniedException, ConstraintViolationException, NoSuchElementException, DuplicateKeyException;

    /**
     * Remove a guest from a room
     *
     * @param room the room
     * @param member member to remove from guests
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     * @throws NoSuchElementException if the room cannot be find or the member
     * is not a guest
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#room, 'owns') or authentication.name == #member.id")
    void removeGuest(
            @NotNull Room room,
            @NotNull Member member)
            throws AccessDeniedException, ConstraintViolationException, NoSuchElementException;
}
