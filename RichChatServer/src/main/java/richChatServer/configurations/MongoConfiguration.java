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
package richChatServer.configurations;

import jakarta.validation.Validator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.mapping.event.ValidatingEntityCallback;
import richChatServer.model.Member;
import richChatServer.security.authentication.MemberAuditorAware;
import richChatServer.security.authentication.RichChatCurrentUserInformationService;

/**
 * Configure Mongo pour utiliser la validation des entités et activer l'audit.
 *
 * @author Rémi Venant
 */
@Configuration
@EnableMongoAuditing // Active l'audit des entité pour injection auto d'instants de création/modif et user créateur/modificateur
public class MongoConfiguration {

    /**
     * Activation de la validation des entité
     *
     * @param validator
     * @return
     */
    @Bean
    public ValidatingEntityCallback validatingEntityCallback(Validator validator) {
        return new ValidatingEntityCallback(validator);
    }

    /**
     * Pour l'Auditing, fourniture d'un bean de récupération du membre courant
     *
     * @param currentUserInfoSvc
     * @return
     */
    @Bean
    public AuditorAware<Member> myAuditorProvider(RichChatCurrentUserInformationService currentUserInfoSvc) {
        return new MemberAuditorAware(currentUserInfoSvc);
    }
}
