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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomMessage;
import richChatServer.model.projections.RoomMessageBasic;

/**
 *
 * @author Rémi Venant
 */
public interface RoomMessageService {

    /**
     * Stream all message of a room, sorted by creation ASC
     *
     * @param room the room
     * @return the stream of the messages
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#room, 'access')")
    List<RoomMessageBasic> getRoomMessages(
            @NotNull Room room)
            throws AccessDeniedException, ConstraintViolationException;

    /**
     * Add a new message to the room.
     *
     * @param room the room
     * @param message the message content
     * @return the created room message
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#room, 'access')")
    RoomMessage addMessageToRoom(
            @NotNull Room room,
            @NotBlank String message)
            throws AccessDeniedException, ConstraintViolationException;

    /**
     * Delete a message from the room.
     *
     * @param room the room
     * @param messageId the message id
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     * @throws NoSuchElementException if the messageId does not exist or is not
     * related to the room
     */
    @PreAuthorize("hasRole('ADMIN') or hasPermission(#room, 'owns')")
    void deleteRoomMessage(
            @NotNull Room room,
            @NotNull @Pattern(regexp = "[abcdef0-9]{24}", flags = Pattern.Flag.CASE_INSENSITIVE) String messageId)
            throws AccessDeniedException, ConstraintViolationException, NoSuchElementException;

    /**
     * Delete a message from a given author from the room.
     *
     * @param room the room
     * @param author the expected author
     * @param messageId the message id
     * @throws AccessDeniedException if not authorized
     * @throws ConstraintViolationException if a parameter is invalid
     * @throws NoSuchElementException if the messageId does not exist or is not
     * related to the room
     */
    @PreAuthorize("hasRole('ADMIN') or (hasPermission(#room, 'access') and authentication.name == #author.id) or hasPermission(#room, 'owns')")
    void deleteRoomMessageOfAuthor(
            @NotNull Room room,
            @NotNull Member author,
            @NotNull @Pattern(regexp = "[abcdef0-9]{24}", flags = Pattern.Flag.CASE_INSENSITIVE) String messageId)
            throws AccessDeniedException, ConstraintViolationException, NoSuchElementException;

}
