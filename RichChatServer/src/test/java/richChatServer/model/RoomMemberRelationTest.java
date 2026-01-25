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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.test.context.ActiveProfiles;
import richChatServer.testConfig.MongoTestConfig;
import richChatServer.testConfig.MongoTestConfig.MockRichChatCurrentUserInformationService;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import({MongoTestConfig.class})
@ActiveProfiles("mongo-test")
public class RoomMemberRelationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockRichChatCurrentUserInformationService mockUserInfoSvc;

    public RoomMemberRelationTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), Room.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
    }

    @Test
    public void testRoomToOwnerRelationTest() {
        System.out.println("room->owner relation test");
        //1 Room, 1 owner creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Room r = this.mongoTemplate.save(new Room("name", "color"));

        System.out.println("Retrieve room: check that 1 query on rooms and 1 query on member is applied");
        Room testedRoom = this.mongoTemplate.findById(r.getId(), Room.class);
        assert testedRoom != null;
        System.out.println("Get owner: no query should be done");
        Member author = testedRoom.getOwner();
        assertThat(author).as("Author is not null and equal to the expected author").isNotNull().isEqualTo(m);
    }

    @Test
    public void testRoomToGuestRelationTest() {
        System.out.println("room->guest relation test");
        //1 Room, 1 owner, 4 guest creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        Member m3 = this.mongoTemplate.save(new Member("mail3@mail.com", "username3"));
        Room r = new Room("name", "color");
        r.setGuests(Stream.of(m2, m3).map(g -> new RoomGuest(g, Boolean.TRUE)).collect(Collectors.toSet()));
        this.mongoTemplate.save(r);

        System.out.println("Retrieve room: only one request");
        Room testedRoom = this.mongoTemplate.findById(r.getId(), Room.class);
        assertThat(testedRoom.getGuests()).as("guest not null and has prper size").hasSize(2);

    }

    @Test
    public void testOwnerToRoomsRelationTest() {
        System.out.println("room->owner relation test");
        //3 Rooms, 2 owner creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Room r = this.mongoTemplate.save(new Room("name", "color"));
        Room r2 = this.mongoTemplate.save(new Room("name", "color"));
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        mockUserInfoSvc.setCurrentMember(m2);
        Room r3 = this.mongoTemplate.save(new Room("name", "color"));

        System.out.println("Find owner 1: Check that only one request to member has been done");
        Member owner = this.mongoTemplate.findById(m.getId(), Member.class);
        assert owner != null;

        System.out.println("Get owned rooms");
        Set<Room> rooms = owner.getOwnedRooms();
        System.out.println("Check rooms size: check that 2 requests on room has been done");
        assertThat(rooms).as("Owner's rooms are not null and size 2").hasSize(2);

        System.out.println("Get all room id");
        List<String> roomIds = rooms.stream().map(Room::getId).toList();
        System.out.println("Check room id");
        assertThat(roomIds).as("Rooms match proper ids").containsExactlyInAnyOrder(r.getId(), r2.getId());
        System.out.println("done");
    }

    @Test
    public void testGuestToRoomRelationTest() {
        System.out.println("guest->rooms relation test");
        //1 Room, 1 owner, 4 guest creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        Member m3 = this.mongoTemplate.save(new Member("mail3@mail.com", "username3"));
        Member m4 = this.mongoTemplate.save(new Member("mail4@mail.com", "username4"));
        Room r = new Room("name", "color");
        r.setGuests(Stream.of(m2, m3).map(g -> new RoomGuest(g, Boolean.TRUE)).collect(Collectors.toSet()));
        this.mongoTemplate.save(r);

        System.out.println("Find guest 2: Check that only one request to member has been done");
        Member guest = this.mongoTemplate.findById(m2.getId(), Member.class);
        assert guest != null;

        System.out.println("Get invited rooms");
        Set<Room> rooms = guest.getInvitedRooms();
        System.out.println("Check rooms size: check that 1 request on room has been done");
        assertThat(rooms).as("Owner's rooms are not null and size 1").hasSize(1);
        System.out.println("Get the invited room");
        Room invitedRoom = rooms.stream().findAny().get();
        System.out.println("Get the invited room owner: check that 1 request on member has been done");
        Member invitedRoomOwner = invitedRoom.getOwner();
        assertThat(invitedRoomOwner).as("Owner not null and ok").isEqualTo(m);
    }
}
