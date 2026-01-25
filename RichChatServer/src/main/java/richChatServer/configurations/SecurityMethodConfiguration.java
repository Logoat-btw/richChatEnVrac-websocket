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
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import richChatServer.security.authorizations.RichChatPermissionEvaluator;

/**
 * Configuration de la gestion des authorisation sur méthode de service
 *
 * @author Rémi Venant
 */
@Configuration
//@EnableMethodSecurity(prePostEnabled = true) // pas utile ?
public class SecurityMethodConfiguration {

    @Bean
    public DefaultMethodSecurityExpressionHandler methodExpressionHandler(PermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler dmse = new DefaultMethodSecurityExpressionHandler();
        dmse.setPermissionEvaluator(permissionEvaluator);
        return dmse;
    }

    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new RichChatPermissionEvaluator();
    }
}
