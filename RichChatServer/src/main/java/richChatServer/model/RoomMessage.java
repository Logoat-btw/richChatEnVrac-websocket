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
package richChatServer.model;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Objects;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import richChatServer.model.views.RoomMessageViews;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "roomMessages")
public class RoomMessage {

    @JsonView(RoomMessageViews.Normal.class)
    @Id
    private String id;

    @JsonView(RoomMessageViews.Detailled.class)
    @Indexed(unique = false)
    @NotNull
    @DocumentReference(lazy = true)
    private Room room;

    @JsonView(RoomMessageViews.Normal.class)
    @NotBlank
    private String message;

    @JsonView(RoomMessageViews.Normal.class)
    @CreatedDate
    private Instant creation;

    @JsonView(RoomMessageViews.Detailled.class)
    @CreatedBy
    @NotNull
    @DocumentReference(lazy = true)
    private Member author;

    protected RoomMessage() {
    }

    public RoomMessage(Room room, String message) {
        this.room = room;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public Room getRoom() {
        return room;
    }

    protected void setRoom(Room room) {
        this.room = room;
    }

    public String getMessage() {
        return message;
    }

    protected void setMessage(String message) {
        this.message = message;
    }

    public Instant getCreation() {
        return creation;
    }

    protected void setCreation(Instant creation) {
        this.creation = creation;
    }

    public Member getAuthor() {
        return author;
    }

    protected void setAuthor(Member author) {
        this.author = author;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoomMessage other = (RoomMessage) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "RoomMessage{" + "id=" + id + ", creation=" + creation + '}';
    }

}
