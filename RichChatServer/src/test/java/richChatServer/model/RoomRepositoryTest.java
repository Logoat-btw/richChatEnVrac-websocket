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
import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
import richChatServer.model.projections.RoomBasic;
import richChatServer.testConfig.MongoTestConfig;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import({MongoTestConfig.class})
@ActiveProfiles("mongo-test")
public class RoomRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoTestConfig.MockRichChatCurrentUserInformationService mockUserInfoSvc;

    @Autowired
    private RoomRepository roomRepo;

    private Member[] members;

    private String[] roomIds;

    public RoomRepositoryTest() {
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
        Member m0 = this.mongoTemplate.save(new Member("mail0@mail.com", "username0"));
        Member m1 = this.mongoTemplate.save(new Member("mail1@mail.com", "username1"));
        Member m2 = this.mongoTemplate.save(new Member("mail2@mail.com", "username2"));

        // 3 rooms
        this.roomIds = new String[3];
        int curIdx = 0;
        // r0: owned by m0, guests: m1, m2
        mockUserInfoSvc.setCurrentMember(m0);
        Room r = new Room("name", "color");
        r.setGuests(Stream.of(m1, m2).map(g -> new RoomGuest(g, Boolean.FALSE)).collect(Collectors.toSet()));
        r = this.mongoTemplate.save(r);
        this.roomIds[curIdx++] = r.getId();
        // r1: owned by m0: guests: m1
        mockUserInfoSvc.setCurrentMember(m0);
        r = new Room("name", "color");
        r.setGuests(Stream.of(m1).map(g -> new RoomGuest(g, Boolean.FALSE)).collect(Collectors.toSet()));
        r = this.mongoTemplate.save(r);
        this.roomIds[curIdx++] = r.getId();
        // r2: owned by m1: guests: m0, m2
        mockUserInfoSvc.setCurrentMember(m1);
        r = new Room("name", "color");
        r.setGuests(Stream.of(m0, m2).map(g -> new RoomGuest(g, Boolean.FALSE)).collect(Collectors.toSet()));
        r = this.mongoTemplate.save(r);
        this.roomIds[curIdx++] = r.getId();

        // reset userInfoSvc
        mockUserInfoSvc.setCurrentMember(null);
        // Create members array
        this.members = new Member[]{m0, m1, m2};
    }

    @AfterEach
    public void tearDown() {
        this.mongoTemplate.remove(new BasicQuery("{}"), Room.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
    }

    /**
     * Test of streamByOwner method, of class RoomRepository.
     */
    @Test
    public void testStreamByOwner() {
        System.out.println("streamByOwner: check that 1 request to rooms has been done");
        Stream<Room> rs = this.roomRepo.streamByOwner(this.members[0]);
        System.out.println("to list: check that several requests to member has been done to retrieve owner and guests");
        List<Room> rooms = rs.toList();
        assertThat(rooms).as("Proper rooms size").hasSize(2);
    }

    /**
     * Test of streamBasicByOwner method, of class RoomRepository.
     */
    @Test
    public void testFindBasicByOwner() {
        System.out.println("findBasicByOwner: check that 1 request to rooms has been done and serveral request to fetch owner");
        Collection<RoomBasic> rooms = this.roomRepo.findBasicByOwnerOrderByName(this.members[0]);
        assertThat(rooms).as("Proper rooms size").hasSize(2);
    }

    /**
     * Test of streamBasicByGuestsMember method, of class RoomRepository.
     */
    @Test
    public void testFindBasicByGuestsMemberOrderByName() {
        System.out.println("findBasicByGuestsMemberOrderByName: check that 1 request to rooms has been done and serveral request to fetch owner");
        Collection<RoomBasic> rooms = this.roomRepo.findBasicByGuestsMemberOrderByName(this.members[1]);
        System.out.println("Check size");
        assertThat(rooms).as("Proper rooms size").hasSize(2);
    }

    @Test
    public void testExistsByIdAndOwnerOrGuestsMember() {
        // Test with owner
        boolean res = this.roomRepo.existsByIdAndOwnerOrGuestsMember(this.roomIds[0], this.members[0].getId());
        assertThat(res).as("Owner can access room").isTrue();

        res = this.roomRepo.existsByIdAndOwnerOrGuestsMember(this.roomIds[0], this.members[1].getId());
        assertThat(res).as("Guest can access room").isTrue();

        res = this.roomRepo.existsByIdAndOwnerOrGuestsMember(this.roomIds[1], this.members[2].getId());
        assertThat(res).as("Nor owner nor guest cannot access room").isFalse();
    }
}
