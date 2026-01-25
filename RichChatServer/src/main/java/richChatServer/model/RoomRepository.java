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

import java.util.Collection;
import java.util.stream.Stream;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import richChatServer.model.projections.RoomBasic;

/**
 *
 * @author Rémi Venant
 */
public interface RoomRepository extends MongoRepository<Room, String> {

    @Query(value = "{id: ?0, $or: [{owner: ?#{new org.bson.types.ObjectId([1])} }, {'guests.member': ?#{new org.bson.types.ObjectId([1])} } ]}", exists = true)
    boolean existsByIdAndOwnerOrGuestsMember(String roomId, String userId);

    Stream<Room> streamByOwner(Member owner);

    Collection<RoomBasic> findBasicByOwnerOrderByName(Member owner);

    Collection<RoomBasic> findBasicByGuestsMemberOrderByName(Member member);

    @Query("{id: ?0, 'guests.member': ?#{new org.bson.types.ObjectId([1].id)} }")
    @Update("{ '$set': { 'guests.$.pending': ?2 } }")
    long findAndSetGuestPendingByIdAndGuestMember(String roomId, Member guest, Boolean pending);

    @Update("{ '$addToSet' : { 'guests' : ?1 } }")
    long findAndPushGuestById(String roomId, RoomGuest guest);

    @Update("{ '$pull' : { 'guests' : { member: ?#{new org.bson.types.ObjectId([1].id)} } } }")
    long findAndPullGuestById(String roomId, Member member);

    int deleteRoomById(String roomId);
}
