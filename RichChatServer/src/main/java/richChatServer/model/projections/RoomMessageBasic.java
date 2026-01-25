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
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import richChatServer.model.views.RoomMessageViews;

/**
 *
 * @author Rémi Venant
 */
public interface RoomMessageBasic {

    @JsonView(RoomMessageViews.Normal.class)
    String getId();

    @JsonView(RoomMessageViews.Normal.class)
    String getMessage();

    @JsonView(RoomMessageViews.Normal.class)
    Instant getCreation();

    @JsonView(RoomMessageViews.Normal.class)
    @Value("#{target.room.id}")
    String getRoomId();

    @JsonView(RoomMessageViews.Normal.class)
    @Value("#{target.author.id}")
    String getAuthorId();
}
