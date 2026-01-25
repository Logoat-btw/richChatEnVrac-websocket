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
public class RoomTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MockRichChatCurrentUserInformationService mockUserInfoSvc;

    public RoomTest() {
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
    public void testRoomCreationTest() {
        System.out.println("room creation test");
        //1 Room, 1 owner creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Room r = this.mongoTemplate.save(new Room("name", "color"));
        assertThat(r.getId()).as("Room created has an id").isNotNull();
        assertThat(r.getOwner()).as("Room created has the proper author").isNotNull().isEqualTo(m);
    }

    @Test
    public void testRoomSetAllGuestsCreationTest() {
        System.out.println("room with all guest creation test");
        //1 Room, 1 owner, 2 guest creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        Member m3 = this.mongoTemplate.save(new Member("mail3@mail.com", "username3"));

        Room r = new Room("name", "color");
        System.out.println("Set all guests with repetition to ensure set is used");
        r.setGuests(Stream.of(m2, m3, m2, m3).map(g -> new RoomGuest(g, Boolean.TRUE)).collect(Collectors.toSet()));
        r = this.mongoTemplate.save(r);

        assertThat(r.getId()).as("Room created has an id").isNotNull();
        System.out.println("No query should be hit to get guest");
        assertThat(r.getGuests()).as("Room created has the guest").hasSize(2);
    }

    @Test
    public void testRoomAddGuestCreationTest() {
        System.out.println("room with add guest creation test");
        //1 Room, 1 owner, 2 guest creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        Member m3 = this.mongoTemplate.save(new Member("mail3@mail.com", "username3"));

        Room r = new Room("name", "color");
        System.out.println("Add guests with repetition to ensure set is used");
        r.getGuests().add(new RoomGuest(m2, Boolean.TRUE));
        r.getGuests().add(new RoomGuest(m3, Boolean.TRUE));
        r.getGuests().add(new RoomGuest(m2, Boolean.TRUE));
        r.getGuests().add(new RoomGuest(m3, Boolean.TRUE));
        r = this.mongoTemplate.save(r);

        assertThat(r.getId()).as("Room created has an id").isNotNull();
        System.out.println("No query should be hit to get guest");
        assertThat(r.getGuests()).as("Room created has the guest").hasSize(2);
    }

    @Test
    public void testRoomSetAllGuestsUpdatingTest() {
        System.out.println("room updating all guest test");
        //1 Room, 1 owner, 4 guest creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        Member m3 = this.mongoTemplate.save(new Member("mail3@mail.com", "username3"));
        Member m4 = this.mongoTemplate.save(new Member("mail4@mail.com", "username4"));
        Member m5 = this.mongoTemplate.save(new Member("mail5@mail.com", "username5"));
        Room r = new Room("name", "color");
        r.setGuests(Stream.of(m2, m3).map(g -> new RoomGuest(g, Boolean.TRUE)).collect(Collectors.toSet()));
        this.mongoTemplate.save(r);

        System.out.println("Retrieve room");
        Room testedRoom = this.mongoTemplate.findById(r.getId(), Room.class);
        assert testedRoom != null;
        System.out.println("Set new guests");
        r.setGuests(Stream.of(m2, m4).map(g -> new RoomGuest(g, Boolean.TRUE)).collect(Collectors.toSet()));

        System.out.println("Saving room");
        testedRoom = this.mongoTemplate.save(testedRoom);
        System.out.println("Saving ok");
    }

    @Test
    public void testRoomUpdateGuestsUpdatingTest() {
        System.out.println("room updating some guest test");
        //1 Room, 1 owner, 4 guest creation
        Member m = this.mongoTemplate.save(new Member("mail@mail.com", "username"));
        mockUserInfoSvc.setCurrentMember(m);
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        Member m3 = this.mongoTemplate.save(new Member("mail3@mail.com", "username3"));
        Member m4 = this.mongoTemplate.save(new Member("mail4@mail.com", "username4"));
        Room r = new Room("name", "color");
        r.setGuests(Stream.of(m2, m3).map(g -> new RoomGuest(g, Boolean.TRUE)).collect(Collectors.toSet()));
        this.mongoTemplate.save(r);

        System.out.println("Retrieve room");
        Room testedRoom = this.mongoTemplate.findById(r.getId(), Room.class);
        assert testedRoom != null;
        System.out.println("Get guests: ");
        Set<RoomGuest> guests = testedRoom.getGuests();
        assertThat(guests).as("Guest has size 2").hasSize(2);
        System.out.println("Add m2 (repetition), remove m3, add m4");
        boolean addResult = guests.add(new RoomGuest(m2, Boolean.FALSE));
        assertThat(addResult).as("Adding repeted user does not add").isFalse();
        addResult = guests.add(new RoomGuest(m4, Boolean.FALSE));
        assertThat(addResult).as("Adding user ok").isTrue();
        addResult = guests.remove(new RoomGuest(m3, Boolean.FALSE));
        assertThat(addResult).as("Removing m3 event with bad pending ok").isTrue();

        System.out.println("Saving room");
        testedRoom = this.mongoTemplate.save(testedRoom);
        System.out.println("Saving ok");
    }
}
