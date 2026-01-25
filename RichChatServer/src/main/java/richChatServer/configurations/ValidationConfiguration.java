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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;

/**
 * Active la validation des beans (anntotations de validation).
 *
 * @author Rémi Venant
 */
@Configuration
public class ValidationConfiguration {

    /**
     * Bean global de gestion de la validation (utilisé pour les entités mongo
     * et les méthode de services)
     *
     * @return
     */
    @Bean
    public LocalValidatorFactoryBean localValidatorFactoryBean() {
        //a common bean to enforce javax validation constraints
        return new LocalValidatorFactoryBean();
    }

    /**
     * Bean de gestion de la validation des méthodes
     *
     * @return
     */
    @Bean
    public MethodValidationPostProcessor validationPostProcessor() {
        return new MethodValidationPostProcessor();
    }
}
