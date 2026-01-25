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

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 *
 * @author Remi Venant
 */
@ConfigurationProperties(prefix = "app.security")
@Validated
public class AppSecurityProperties {

    private final boolean csrf;

    private final boolean corsDev;

    @NotBlank
    private final String localAdmin;

    public AppSecurityProperties(boolean csrf, boolean corsDev, String localAdmin) {
        this.csrf = csrf;
        this.corsDev = corsDev;
        this.localAdmin = localAdmin;
    }

    public boolean isCorsDev() {
        return corsDev;
    }

    public boolean isCsrf() {
        return csrf;
    }

    public String getLocalAdmin() {
        return localAdmin;
    }

}
