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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import richChatServer.model.views.MemberViews;

/**
 *
 * @author Rémi Venant
 */
@Document(collection = "members")
public class Member {

    @JsonView(MemberViews.Normal.class)
    @Id
    private String id;

    @JsonView(MemberViews.Detailled.class)
    @NotBlank
    @Email
    @Size(min = 1, max = 100)
    @Indexed(unique = true)
    private String email;

    @JsonView(MemberViews.Normal.class)
    @NotBlank
    @Size(min = 1, max = 100)
    private String username;

    @JsonIgnore
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'owner':?#{#self._id} }", lazy = true)
    Set<Room> ownedRooms;

    @JsonIgnore
    @ReadOnlyProperty
    @DocumentReference(lookup = "{'guests.member':?#{#self._id} }", lazy = true)
    Set<Room> invitedRooms;

    protected Member() {
    }

    public Member(String email, String username) {
        this.email = email;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    protected void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Room> getOwnedRooms() {
        return this.ownedRooms == null ? Set.of() : Collections.unmodifiableSet(this.ownedRooms);
    }

    public Set<Room> getInvitedRooms() {
        return this.invitedRooms == null ? Set.of() : Collections.unmodifiableSet(this.invitedRooms);
    }

    @Override
    public String toString() {
        return "Member{" + "id=" + id + ", email=" + email + ", username=" + username + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.id);
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
        final Member other = (Member) obj;
        return Objects.equals(this.id, other.id);
    }

}
