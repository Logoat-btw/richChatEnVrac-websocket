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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.test.context.ActiveProfiles;
import richChatServer.testConfig.MongoTestConfig;

/**
 *
 * @author Rémi Venant
 */
@DataMongoTest
@Import({MongoTestConfig.class})
@ActiveProfiles("mongo-test")
public class MemberCredentialTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    public MemberCredentialTest() {
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
        this.mongoTemplate.remove(new BasicQuery("{}"), MemberCredential.class);
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
    }

    @Test
    public void testCredentialCreation() {
        System.out.println("test Credential Creation");
        Member member = this.mongoTemplate.save(new Member("member1@mail.com", "username"));
        MemberCredential mcToCreate = new MemberCredential(member, "pwdHash");
        MemberCredential savecMc = this.mongoTemplate.save(mcToCreate);
        assertThat(savecMc.getId()).as("entity created has an id").isNotNull();
        assertThat(savecMc).extracting("encodedPassword")
                .isEqualTo(mcToCreate.getEncodedPassword());
    }

    @Test
    public void testCredentialUnicityPerMember() {
        System.out.println("test Credential Unicity Per Member");
        Member member1 = this.mongoTemplate.save(new Member("member1@mail.com", "username"));
        Member member2 = this.mongoTemplate.save(new Member("member2@mail.com", "username"));
        MemberCredential mcMem1 = this.mongoTemplate.save(new MemberCredential(member1, "pwdHash"));
        MemberCredential mcMem2 = this.mongoTemplate.save(new MemberCredential(member2, "pwdHash"));
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new MemberCredential(member1, "pwdHash")))
                .as("Duplicated credential per member rejected")
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void testProperMemberlReferenceAndLazy() {
        System.out.println("test Proper Memberl Reference And Lazy");
        Member member1 = this.mongoTemplate.save(new Member("member1@mail.com", "username"));
        MemberCredential mcMem1 = this.mongoTemplate.save(new MemberCredential(member1, "pwdHash"));
        System.out.println("-".repeat(10) + " PLEASE CHECK LOG FOR DOCUMENT CREATION " + "-".repeat(10));

        MemberCredential mc = this.mongoTemplate.findById(mcMem1.getId(), MemberCredential.class);
        assertThat(mc).as("MemberCred retrieved ok").isNotNull();
        assert mc != null;
        System.out.println("-".repeat(10) + " PLEASE CHECK LOG FOR ONLY ONE FIND REQUEST ON MemberCredentials collection" + "-".repeat(10));

        Member mem = mc.getMember();
        assertThat(mem).as("Proper member retrieve").isEqualTo(member1);
        System.out.println("-".repeat(10) + " PLEASE CHECK LOG FOR ONLY ONE FIND REQUEST ON Members collection " + "-".repeat(10));
    }

}
