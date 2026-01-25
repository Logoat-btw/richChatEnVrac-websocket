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
package richChatServer.security.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Un encoder évolutif, permettant de mettre à jour automatiquement à un vieux hash de mot de passe à la prochaine authentification.
 * @author Remi Venant
 */
public class EvolutivePasswordEncoder implements PasswordEncoder {

    private static final Log LOG = LogFactory.getLog(EvolutivePasswordEncoder.class);

    @Override
    public String encode(CharSequence rawPassword) {
        return this.encodeWithAllowedAlgorithm(rawPassword, AllowedPasswordEncoder.getDefault());
    }

    public String encodeWithAllowedAlgorithm(CharSequence rawPassword, AllowedPasswordEncoder encoder) {
        return createEvolutiveEncodedPassword(encoder.getEncoder().encode(rawPassword), encoder);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String evolutiveEncodedPassword) {
        final EncodedSystem es = unmergeEvolutiveEncodePassword(evolutiveEncodedPassword);
        return es.encoder.getEncoder().matches(rawPassword, es.encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String evolutiveEncodedPassword) {
        final EncodedSystem es = unmergeEvolutiveEncodePassword(evolutiveEncodedPassword);
        return es.encoder.getEncoder().upgradeEncoding(es.encodedPassword);
    }

    private static String createEvolutiveEncodedPassword(String encodedPassword, AllowedPasswordEncoder encoder) {
        if (encodedPassword == null || encoder == null) {
            throw new NullPointerException();
        }
        final String encoderKey = encoder.name();
        return encoderKey + "." + encodedPassword;
    }

    private static EncodedSystem unmergeEvolutiveEncodePassword(String evolutiveEncodePassword) {
        final String[] parts = evolutiveEncodePassword.split("\\.", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Bad serialized encoded password");
        }
        AllowedPasswordEncoder pwdEncoder = AllowedPasswordEncoder.valueOf(parts[0]);
        return new EncodedSystem(parts[1], pwdEncoder);
    }

    private static class EncodedSystem {

        private final String encodedPassword;
        private final AllowedPasswordEncoder encoder;

        public EncodedSystem(String encodedPassword, AllowedPasswordEncoder encoder) {
            this.encodedPassword = encodedPassword;
            this.encoder = encoder;
        }
    }

}
