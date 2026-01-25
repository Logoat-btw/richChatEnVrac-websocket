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
package richChatServer.testConfig;

import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import richChatServer.configurations.MongoConfiguration;
import richChatServer.configurations.ValidationConfiguration;
import richChatServer.model.Member;
import richChatServer.model.MemberCredential;
import richChatServer.security.authentication.RichChatCurrentUserInformationService;
import richChatServer.security.authentication.RichChatCurrentUserInformationServiceImpl;
import richChatServer.security.authentication.RichChatMemberDetails;

/**
 *
 * @author Rémi Venant
 */
@TestConfiguration
@Import({ValidationConfiguration.class, MongoConfiguration.class})
public class MongoTestConfig {

    @Bean
    @ConditionalOnMissingBean
    public RichChatCurrentUserInformationService richChatCurrentUserInformationService() {
        return new MockRichChatCurrentUserInformationService();
    }

    public static class MockRichChatCurrentUserInformationService implements RichChatCurrentUserInformationService {

        private Member currentMember;

        public Member getCurrentMember() {
            return currentMember;
        }

        public void setCurrentMember(Member currentMember) {
            this.currentMember = currentMember;
        }

        @Override
        public Optional<RichChatMemberDetails> getOptionalUser() {
            if (this.currentMember == null) {
                return Optional.empty();
            } else {
                return Optional.of(new RichChatMemberDetails(currentMember, new MemberCredential(currentMember, "####"), false));
            }
        }

        @Override
        public String getUsername() {
            return this.currentMember == null ? null : this.currentMember.getId();
        }

    }
}
