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
package richChatServer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import richChatServer.model.Member;
import richChatServer.model.MemberCredential;
import richChatServer.model.MemberCredentialRepository;
import richChatServer.model.MemberRepository;
import richChatServer.model.Room;
import richChatServer.model.RoomGuest;
import richChatServer.model.RoomMessage;
import richChatServer.model.RoomMessageRepository;
import richChatServer.model.RoomRepository;
import richChatServer.model.SampleDataForgeUtility;

/**
 *
 * @author Rémi Venant
 */
@Profile("sample-data && development")
@Component
public class SampleDataLoader implements CommandLineRunner {

    private static final Log LOG = LogFactory.getLog(SampleDataLoader.class);

    private final MemberRepository memberRepo;

    private final MemberCredentialRepository memberCredRepo;

    private final RoomRepository roomRepo;

    private final RoomMessageRepository roomMsgRepo;

    private final Optional<PasswordEncoder> passwordEncoder;

    private final SampleDataForgeUtility sampleDataForgeUtility;

    @Autowired
    public SampleDataLoader(MemberRepository memberRepo, MemberCredentialRepository memberCredRepo,
            RoomRepository roomRepo, RoomMessageRepository roomMsgRepo,
            Optional<PasswordEncoder> passwordEncoder, SampleDataForgeUtility sampleDataForgeUtility) {
        this.memberRepo = memberRepo;
        this.memberCredRepo = memberCredRepo;
        this.roomRepo = roomRepo;
        this.roomMsgRepo = roomMsgRepo;
        this.passwordEncoder = passwordEncoder;
        this.sampleDataForgeUtility = sampleDataForgeUtility;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("SAMPL DATA LOADER: start...");
        if (this.memberCredRepo.count() > 0) {
            LOG.info("Data present in dabase. Stop sample loader.");
        } else {
            this.clearDb();
            final List<Member> members = this.createMembers();
            this.createCredentials(members);
            final List<Room> rooms = this.createRooms(members);
            this.createRoomMessages(rooms, members);
        }
        LOG.info("SAMPL DATA LOADER: end.");
        LOG.info("You can login to the server using one of the given credential:");
        Stream.of("sophie", "prudence", "annie").forEach((username) -> {
            LOG.info(String.format("- email: %s@mail.com | password: %s", username, username));
        });
    }

    private void clearDb() {
        this.roomMsgRepo.deleteAll();
        this.roomRepo.deleteAll();
        this.memberCredRepo.deleteAll();
        this.memberRepo.deleteAll();
    }

    /**
     * Create 3 members: sophie, prudence, annie. For all of them, the mail will
     * be [username]@mail.com
     *
     * @return list of created members
     */
    private List<Member> createMembers() {
        final List<Member> membersToCreate = Stream.of("sophie", "prudence", "annie")
                .map(username -> new Member(String.format("%s@mail.com", username), username))
                .toList();
        return this.memberRepo.saveAll(membersToCreate);
    }

    /**
     * Create credential for all users using their username as a password.
     *
     * @return list of created credentials
     */
    private List<MemberCredential> createCredentials(List<Member> members) {
        final List<MemberCredential> credToCreate = members.stream()
                .map((member) -> new MemberCredential(
                member,
                this.passwordEncoder.map(pe -> pe.encode(member.getUsername()))
                        .orElse(member.getUsername())
        ))
                .toList();
        return this.memberCredRepo.saveAll(credToCreate);
    }

    /**
     * Create 2 rooms. One owned by sophie with guests: prudence, annie
     * (pending) (pending) One owned by prudence with guest: annie
     *
     * @param members the members
     * @return list of created rooms
     */
    private List<Room> createRooms(List<Member> members) {
        Room r1 = this.sampleDataForgeUtility.withOwner(new Room("First sophie's room", "#49dd9f"), members.get(0));
        r1.setGuests(Set.of(new RoomGuest(members.get(1), Boolean.FALSE), new RoomGuest(members.get(2), Boolean.TRUE)));
        Room r2 = this.sampleDataForgeUtility.withOwner(new Room("Les animaux préférés de Prudence", null), members.get(1));
        r2.setGuests(Set.of(new RoomGuest(members.get(2), Boolean.FALSE)));
        return this.roomRepo.saveAll(List.of(r1, r2));
    }

    /**
     * For each room create one message for each of the room members (except for
     * pending ones).
     *
     * @param rooms
     * @param members
     * @return list of created message
     */
    private List<RoomMessage> createRoomMessages(List<Room> rooms, List<Member> members) {
        List<RoomMessage> msgToCreate = new ArrayList<>();

        for (Room r : rooms) {
            RoomMessage msg = this.sampleDataForgeUtility.withAuthor(new RoomMessage(r, "Un petit message"),
                    r.getOwner());
            msgToCreate.add(msg);

            for (RoomGuest guest : r.getGuests()) {
                if (!Boolean.TRUE.equals(guest.getPending())) {
                    msg = this.sampleDataForgeUtility.withAuthor(new RoomMessage(r, "<h1>Un autre message</h1>Ceci est un autre petit Message."),
                            guest.getMember());
                    msgToCreate.add(msg);
                }
            }
        }
        return this.roomMsgRepo.saveAll(msgToCreate);
    }
}
