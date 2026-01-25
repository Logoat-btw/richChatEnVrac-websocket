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

import jakarta.validation.ConstraintViolationException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
public class MemberTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    public MemberTest() {
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
        this.mongoTemplate.remove(new BasicQuery("{}"), Member.class);
    }

    @Test
    public void testMemberPersistence() {
        System.out.println("Test Member persistence");
        Member memToSave = new Member("email@mail.com", "username");
        Member savecMem = this.mongoTemplate.save(memToSave);
        assertThat(savecMem.getId()).as("entity created has an id").isNotNull();
        assertThat(savecMem).extracting("email", "username")
                .containsExactly(memToSave.getEmail(), memToSave.getUsername());
    }

    /**
     * Test email validation
     */
    @Test
    public void testEmailValidation() {
        System.out.println("Test Email validation");
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email@mail .com", "username")))
                .as("Invalid email is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email<Injection>@mail.com", "username")))
                .as("Invalid email is rejected (2)")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("e".repeat(100) + "email@mail.com", "username")))
                .as("Too long email is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("", "username")))
                .as("empty email is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("       ", "username")))
                .as("blanck email is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member(null, "username")))
                .as("null email is rejected")
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testUniqueEmail() {
        System.out.println("Test Email unicity");
        this.mongoTemplate.save(new Member("email@mail.com", "username"));
        this.mongoTemplate.save(new Member("email2@mail.com", "username2"));
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email@mail.com", "username3")))
                .as("Duplicated mail rejected")
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void testUsernameValidation() {
        System.out.println("Test username validation");
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email@mail.com", "e".repeat(101))))
                .as("Too long firstname is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email@mail.com", "")))
                .as("Empty firstname is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email@mail.com", "   ")))
                .as("blanck firstname is rejected")
                .isInstanceOf(ConstraintViolationException.class);
        assertThatThrownBy(()
                -> this.mongoTemplate.save(new Member("email@mail.com", null)))
                .as("Null firstname is rejected")
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void testNonUniqueUsername() {
        System.out.println("Test Username non unicity");
        Member m1 = this.mongoTemplate.save(new Member("email@mail.com", "username"));
        Member m2 = this.mongoTemplate.save(new Member("email2@mail.com", "username"));
        assertThat(m1.getId()).as("M1 has an id").isNotNull();
        assertThat(m2.getId()).as("M2 has an id").isNotNull();
        assertThat(m1.getId()).as("M1 does not have the same id than M2").isNotEqualTo(m2.getId());
    }

}
