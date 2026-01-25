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

import java.util.stream.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import richChatServer.model.projections.RoomMessageBasic;

/**
 *
 * @author Rémi Venant
 */
public interface RoomMessageRepository extends MongoRepository<RoomMessage, String> {

    Stream<RoomMessageBasic> streamByRoomOrderByCreationAsc(Room room);

    void deleteByRoom(Room room);

    long deleteByRoomAndId(Room room, String id);
    
    long deleteByRoomAndIdAndAuthor(Room room, String id, Member author);
}
