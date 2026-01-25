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
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import richChatServer.model.views.RoomViews;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "rooms")
public class Room {

    @JsonView(RoomViews.Normal.class)
    @Id
    private String id;

    @JsonView(RoomViews.Normal.class)
    @NotBlank
    private String name;

    @JsonView(RoomViews.Normal.class)
    private String color;

    @JsonView(RoomViews.Normal.class)
    @CreatedBy
    @NotNull
    @DocumentReference
    private Member owner;

    @JsonView(RoomViews.Detailled.class)
    @Valid
    private Set<RoomGuest> guests = new HashSet<>();

    protected Room() {
    }

    public Room(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Member getOwner() {
        return owner;
    }

    public void setOwner(Member owner) {
        this.owner = owner;
    }

    public Set<RoomGuest> getGuests() {
        return this.guests;
    }

    public void setGuests(Set<RoomGuest> guests) {
        this.guests = guests;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.id);
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
        final Room other = (Room) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "Room{" + "id=" + id + ", name=" + name + ", color=" + color + '}';
    }
}
