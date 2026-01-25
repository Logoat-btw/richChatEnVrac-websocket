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
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import richChatServer.model.views.RoomViews;

/**
 *
 * @author Rémi Venant
 */
public class RoomGuest {

    @JsonView(RoomViews.Detailled.class)
    @NotNull
    @DocumentReference
    private Member member;

    @JsonView(RoomViews.Detailled.class)
    private Boolean pending;

    protected RoomGuest() {
    }

    public RoomGuest(Member member, Boolean pending) {
        this.member = member;
        this.pending = pending;
    }

    public Member getMember() {
        return member;
    }

    protected void setMember(Member member) {
        this.member = member;
    }

    public Boolean getPending() {
        return pending;
    }

    public void setPending(Boolean pending) {
        this.pending = pending;
    }

    @Override
    public int hashCode() {
        // We explicitly used member.id to allow Real RoomGuest and proxy equivalence
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.getMember().getId());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        // We explicitly used member.id to allow Real RoomGuest and proxy equivalence
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RoomGuest other = (RoomGuest) obj;
        return Objects.equals(this.getMember().getId(), other.getMember().getId());
    }

}
