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
package richChatServer.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import richChatServer.model.Member;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.RoomMessage;
import richChatServer.model.RoomMessageRepository;
import richChatServer.model.RoomRepository;
import richChatServer.model.projections.RoomBasic;
import richChatServer.testConfig.MongoTestConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;
import richChatServer.model.projections.RoomMessageBasic;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import({MongoTestConfig.class})
@ActiveProfiles({"test", "mongo-test"})
public class RoomServiceImplTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoTestConfig.MockRichChatCurrentUserInformationService mockUserInfoSvc;

    @Autowired
    private RoomRepository roomRepo;

    @Autowired
    private RoomMessageRepository roomMsgRepo;

    private RoomServiceImpl testedSvc;

    private Member[] members;

    private String[] roomIds;

    private String[] msgIds;

    public RoomServiceImplTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        // 3 member (m0, m1, m2)
        this.members = new Member[3];
        int idx = 0;
        Member m0 = this.mongoTemplate.save(new Member("mail0@mail.com", "username0"));
        this.members[idx++] = m0;
        Member m1 = this.mongoTemplate.save(new Member("mail1@mail.com", "username1"));
        this.members[idx++] = m1;
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));
        this.members[idx++] = m2;

        // 2 rooms
        this.roomIds = new String[2];
        idx = 0;
        // r0: owned by m0, guests: m1, m2 (pending)
        mockUserInfoSvc.setCurrentMember(m0);
        Room r = new Room("name", "color");
        r.getGuests().add(new RoomGuest(m1, Boolean.FALSE));
        r.getGuests().add(new RoomGuest(m2, Boolean.TRUE));
        this.mongoTemplate.save(r);
        this.roomIds[idx++] = r.getId();
        // r1: owned by m1: guests: m0
        mockUserInfoSvc.setCurrentMember(m1);
        r = new Room("name", "color");
        r.getGuests().add(new RoomGuest(m0, Boolean.FALSE));
        this.mongoTemplate.save(r);
        this.roomIds[idx++] = r.getId();

        // 2 room Message in r1, one from m1 another from m0
        this.msgIds = new String[2];
        idx = 0;
        mockUserInfoSvc.setCurrentMember(m1);
        RoomMessage msg = this.mongoTemplate.save(new RoomMessage(r, "Message from m1"));
        this.msgIds[idx++] = msg.getId();

        mockUserInfoSvc.setCurrentMember(m0);
        msg = this.mongoTemplate.save(new RoomMessage(r, "Message from m0"));
        this.msgIds[idx++] = msg.getId();

        // reset userInfoSvc
        mockUserInfoSvc.setCurrentMember(null);

        // Create the service
        this.testedSvc = new RoomServiceImpl(roomRepo, roomMsgRepo);
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), RoomMessage.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Room.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
    }

    /**
     * Test of getMemberRooms method, of class RoomServiceImpl.
     */
    @Test
    public void testGetMemberRooms() {
        System.out.println("getMemberRooms");
        List<RoomBasic> rooms = this.testedSvc.getMemberRooms(this.members[0]);
        assertThat(rooms).as("Rooms has size 2").hasSize(2);
        assertThat(rooms.stream().map(RoomBasic::getId)).containsExactly(this.roomIds[0], this.roomIds[1]);
    }

    /**
     * Test of accessRoom method, of class RoomServiceImpl.
     */
    @Test
    public void testAccessRoomAsOwner() {
        System.out.println("accessRoom as owner");
        Room r = this.testedSvc.accessRoom(this.roomIds[0], this.members[0]);
        assertThat(r).as("Room found").isNotNull();
    }

    /**
     * Test of accessRoom method, of class RoomServiceImpl.
     */
    @Test
    public void testAccessRoomAsPendingGuest() {
        System.out.println("accessRoom as pending guest");
        final Member member = this.members[2];
        // Retrieve directly the room to check pending state before access
        System.out.println("Check before access");
        Room r = this.mongoTemplate.findById(this.roomIds[0], Room.class);
        assertThat(r.getGuests().stream().filter(g -> g.getMember().equals(member))
                .findAny().get().getPending()).as("In the initial room, member is pending").isEqualTo(Boolean.TRUE);

        // Access room through service
        System.out.println("Acces");
        r = this.testedSvc.accessRoom(this.roomIds[0], member);
        assertThat(r).as("Room found").isNotNull();
        System.out.println("Check from access result");
        assertThat(r.getGuests().stream().filter(g -> g.getMember().equals(member))
                .findAny().get().getPending()).as("In the retrieved room, member is no more pending").isNotEqualTo(Boolean.TRUE);

        // Retrieve directly the room to check pending state has been effectively updated
        System.out.println("Check after access");
        r = this.mongoTemplate.findById(this.roomIds[0], Room.class);
        assertThat(r.getGuests().stream().filter(g -> g.getMember().equals(member))
                .findAny().get().getPending()).as("In the saved room, member is no more pending").isNotEqualTo(Boolean.TRUE);
    }

    /**
     * Test of createRoom method, of class RoomServiceImpl.
     */
    @Test
    public void testCreateRoom() {
        System.out.println("createRoom");
        mockUserInfoSvc.setCurrentMember(this.members[2]);

        Room r = this.testedSvc.createRoom("myRoom", "myColor");
        assertThat(r.getId()).as("Room has an id").isNotNull();
        assertThat(r).as("Room has propre name and color").extracting("name", "color").containsExactly("myRoom", "myColor");
        assertThat(r.getOwner()).as("Room has an owner and is the expected one").isNotNull().isEqualTo(this.members[2]);
        assertThat(r.getGuests()).as("Room has no guest").isEmpty();
    }

    /**
     * Test of inviteGuest method, of class RoomServiceImpl.
     */
    @Test
    public void testInviteGuest() {
        System.out.println("inviteGuest");
        final Room originalRoom = this.mongoTemplate.findById(this.roomIds[1], Room.class);

        // Check we cannot add the owner as a guest
        System.out.println("Check owner cannot be invited");
        assertThatThrownBy(()
                -> this.testedSvc.inviteGuest(originalRoom, originalRoom.getOwner()))
                .as("Cannot invite the owner")
                .isInstanceOf(DuplicateKeyException.class);

        // Check we cannot add the already guest m0 as a guest
        System.out.println("Check guest cannot be invited");
        assertThatThrownBy(()
                -> this.testedSvc.inviteGuest(originalRoom, this.members[0]))
                .as("Cannot invite an already guest member")
                .isInstanceOf(DuplicateKeyException.class);

        // Add m2 as a guest
        System.out.println("Add the guest");
        RoomGuest guest = this.testedSvc.inviteGuest(originalRoom, this.members[2]);

        // Check the result
        System.out.println("Check result");
        assertThat(guest).as("Guest if member 2 and is pending").extracting("member", "pending").containsExactly(this.members[2], Boolean.TRUE);
        // Ensure local representation of room has been updated
        System.out.println("Check local entity consistency");
        Optional<RoomGuest> entityGuest = originalRoom.getGuests().stream().filter(g -> g.getMember().equals(this.members[2])).findFirst();
        assertThat(entityGuest).as("Local guest is present").isPresent();
        assertThat(entityGuest.get().getPending()).as("Local guest is pending").isEqualTo(Boolean.TRUE);

        // Retrieve from from db  and use the guest is inside with the proper state
        System.out.println("DB State");
        Room r = this.mongoTemplate.findById(this.roomIds[1], Room.class);
        entityGuest = r.getGuests().stream().filter(g -> g.getMember().equals(this.members[2])).findFirst();
        assertThat(entityGuest).as("Guest is present in DB").isPresent();
        assertThat(entityGuest.get().getPending()).as("guest in DB is pending").isEqualTo(Boolean.TRUE);
    }

    /**
     * Test of removeGuest method, of class RoomServiceImpl.
     */
    @Test
    public void testRemoveGuest() {
        System.out.println("removeGuest");
        final Room originalRoom = this.mongoTemplate.findById(this.roomIds[1], Room.class);

        // Check we cannot remove a non guest
        System.out.println("Check owner cannot be invited");
        assertThatThrownBy(()
                -> this.testedSvc.removeGuest(originalRoom, this.members[2]))
                .as("Cannot remove a member from guests who is not a guest")
                .isInstanceOf(NoSuchElementException.class);

        // Remove m0 from guest
        System.out.println("Remove the guest");
        this.testedSvc.removeGuest(originalRoom, this.members[0]);

        // Ensure local representation of room has been updated
        System.out.println("Check local entity consistency");
        Optional<RoomGuest> entityGuest = originalRoom.getGuests().stream().filter(g -> g.getMember().equals(this.members[0])).findFirst();
        assertThat(entityGuest).as("Local guest is absent").isNotPresent();

        // Retrieve from from db  and use the guest is inside with the proper state
        System.out.println("DB State");
        Room r = this.mongoTemplate.findById(this.roomIds[1], Room.class);
        entityGuest = r.getGuests().stream().filter(g -> g.getMember().equals(this.members[0])).findFirst();
        assertThat(entityGuest).as("Guest is absent in DB").isNotPresent();
    }

    /**
     * Test of deleteRoom method, of class RoomServiceImpl.
     */
    @Test
    public void testDeleteRoom() {
        System.out.println("deleteRoom");
        final Room originalRoom = this.mongoTemplate.findById(this.roomIds[1], Room.class);

        this.testedSvc.deleteRoom(originalRoom);

        assertThat(this.roomRepo.count()).as("1 room deleted").isEqualTo(this.roomIds.length - 1);
        assertThat(this.roomMsgRepo.count()).as("Room messages deleted").isEqualTo(this.msgIds.length - 2);
    }

    /**
     * Test of getRoomMessages method, of class RoomServiceImpl.
     */
    @Test
    public void testGetRoomMessages() {
        System.out.println("getRoomMessages");
        final Room originalRoom = this.mongoTemplate.findById(this.roomIds[1], Room.class);

        List<RoomMessageBasic> messages = this.testedSvc.getRoomMessages(originalRoom);
        assertThat(messages).as("2 messages retrieved").hasSize(2);
    }

    /**
     * Test of addMessageToRoom method, of class RoomServiceImpl.
     */
    @Test
    public void testAddMessageToRoom() {
        System.out.println("addMessageToRoom");
        final Room originalRoom = this.mongoTemplate.findById(this.roomIds[1], Room.class);
        mockUserInfoSvc.setCurrentMember(this.members[2]);
        final Instant now = Instant.now();
        RoomMessage message = this.testedSvc.addMessageToRoom(originalRoom, "MSG");
        assertThat(message.getId()).as("Msg created has an id").isNotNull();
        assertThat(message.getMessage()).as("Msg created has proper content").isEqualTo("MSG");
        assertThat(message.getAuthor()).as("Msg created has proper auther").isEqualTo(this.members[2]);
        assertThat(message.getCreation()).as("Msg created has proper auther").isCloseTo(now, Assertions.within(10, ChronoUnit.MILLIS));
    }

    /**
     * Test of deleteRoomMessage method, of class RoomServiceImpl.
     */
    @Test
    public void testDeleteRoomMessage() {
        System.out.println("deleteRoomMessage");
        final Room originalOtherRoom = this.mongoTemplate.findById(this.roomIds[0], Room.class);
        assertThatThrownBy(()
                -> this.testedSvc.deleteRoomMessage(originalOtherRoom, this.msgIds[0]))
                .as("Cannot remove a message if it is not related to the proper room")
                .isInstanceOf(NoSuchElementException.class);

        Room goodRoom = this.mongoTemplate.findById(this.roomIds[1], Room.class);
        this.testedSvc.deleteRoomMessage(goodRoom, this.msgIds[0]);

        assertThat(this.roomMsgRepo.count()).as("One message deleted").isEqualTo(this.msgIds.length - 1);
    }

}
