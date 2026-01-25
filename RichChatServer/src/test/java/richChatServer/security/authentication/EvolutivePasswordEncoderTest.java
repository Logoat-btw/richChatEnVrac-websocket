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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Rémi Venant
 */
public class EvolutivePasswordEncoderTest {

    private EvolutivePasswordEncoder testedEncoder;

    public EvolutivePasswordEncoderTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
        this.testedEncoder = new EvolutivePasswordEncoder();
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testEncodeAndMatchWithDefault() {
        System.out.println("test Encode And Match With Default");
        String rawPwd = "MySuperPass";

        AllowedPasswordEncoder dfltEncoder = AllowedPasswordEncoder.getDefault();
        String dfltEncoderKey = dfltEncoder.name();

        String encodedPwd = this.testedEncoder.encode(rawPwd);
        System.out.println("For password >" + rawPwd + "< encoded pass with default encoder \"" + dfltEncoderKey + "\": >" + encodedPwd + "<");

        assertThat(encodedPwd).as("Encode with default encoded pwd starts with encoder key").startsWith(dfltEncoderKey);

        boolean matchResult = this.testedEncoder.matches(rawPwd, encodedPwd);
        assertThat(matchResult).as("Match on default encoded pwd ok").isTrue();
    }

    @Test
    public void testEncodeWithAllowedAlgorithm() {
        System.out.println("test Encode With Allowed Algorithm");
        String rawPwd = "MySuperPass";

        AllowedPasswordEncoder tstEncoder = AllowedPasswordEncoder.ARGON2;
        String dfltEncoderKey = tstEncoder.name();

        String encodedPwd = this.testedEncoder.encodeWithAllowedAlgorithm(rawPwd, tstEncoder);
        System.out.println("For password >" + rawPwd + "< encoded pass with encoder \"" + dfltEncoderKey + "\": >" + encodedPwd + "<");

        assertThat(encodedPwd).as("Encode with default encoded pwd starts with encoder key").startsWith(dfltEncoderKey);
        boolean matchResult = this.testedEncoder.matches(rawPwd, encodedPwd);
        assertThat(matchResult).as("Match on default encoded pwd ok").isTrue();
    }

}
