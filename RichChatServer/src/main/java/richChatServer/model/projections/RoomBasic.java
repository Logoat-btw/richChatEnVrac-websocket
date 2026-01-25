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
package richChatServer.model.projections;

import com.fasterxml.jackson.annotation.JsonView;
import richChatServer.model.Member;
import richChatServer.model.views.RoomViews;

/**
 *
 * @author Rémi Venant
 */
public interface RoomBasic {

    @JsonView(RoomViews.Normal.class)
    String getId();

    @JsonView(RoomViews.Normal.class)
    String getName();

    @JsonView(RoomViews.Normal.class)
    String getColor();

    @JsonView(RoomViews.Normal.class)
    Member getOwner();
}
