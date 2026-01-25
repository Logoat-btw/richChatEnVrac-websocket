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
package richChatServer.restAccess;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author Rémi Venant
 */
public class TestRestRequestFactory {
    private final String epBase = "http://localhost";

    private final int serverPort;

    private final String baseRestPath;

    private final TestRestTemplate restTemplate;

    private String username;

    private String password;

    private MultiValueMap<String, String> headers;

    private boolean verbose;
    
    public TestRestRequestFactory(int serverPort, String baseRestPath, TestRestTemplate restTemplate) {
        this.serverPort = serverPort;
        this.baseRestPath = baseRestPath;
        this.restTemplate = restTemplate;
    }
    
    public ResponseEntity<JsonNode> getMyself(HttpStatus expectedStatus) {
        this.logIfVerbose("Requesting GET myself REST...");
        final String ep = String.format("%s:%d%s/accounts/myself", epBase, serverPort, baseRestPath);
        ResponseEntity<JsonNode> resp = this.restTemplate.withBasicAuth(username, password).exchange(ep,
                HttpMethod.GET, new HttpEntity<Void>(this.prepareHeaders(headers)), JsonNode.class);
        if (expectedStatus != null) {
            assertThat(resp.getStatusCode()).as("GET myself REST returned expected code").isEqualTo(expectedStatus);
        }
        return resp;
    }
    
    public ResponseEntity<JsonNode> createAccount(String email, String username, String password, HttpStatus expectedStatus) {
        this.logIfVerbose("Requesting POST accounts REST...");
        final String ep = String.format("%s:%d%s/accounts", epBase, serverPort, baseRestPath);
        ResponseEntity<JsonNode> resp = this.restTemplate.exchange(ep,
                HttpMethod.POST, new HttpEntity<Map>(Map.of("email", email, "username", username, "password", password), this.prepareHeaders(headers)), JsonNode.class);
        if (expectedStatus != null) {
            assertThat(resp.getStatusCode()).as("POST accounts REST returned expected code").isEqualTo(expectedStatus);
        }
        return resp;
    }
    
    public int getServerPort() {
        return serverPort;
    }

    public String getBaseRestPath() {
        return baseRestPath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public TestRestRequestFactory withUsername(String username) {
        this.setUsername(username);
        return this;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TestRestRequestFactory withPassword(String password) {
        this.setPassword(password);
        return this;
    }

    public MultiValueMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(MultiValueMap<String, String> headers) {
        this.headers = headers;
    }

    public TestRestRequestFactory withHeaders(MultiValueMap<String, String> headers) {
        this.setHeaders(headers);
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public TestRestRequestFactory withVerbose(boolean verbose) {
        this.setVerbose(verbose);
        return this;
    }

    private void logIfVerbose(String message) {
        if (verbose) {
            System.out.println(message);
        }
    }

    private HttpHeaders prepareHeaders(MultiValueMap<String, String> extraHeaders) {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "Application/json");
        if (extraHeaders != null && !extraHeaders.isEmpty()) {
            httpHeaders.addAll(extraHeaders);
        }
        return httpHeaders;
    }
}
