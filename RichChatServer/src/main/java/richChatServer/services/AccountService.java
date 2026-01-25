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
package richChatServer.services;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.NoSuchElementException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import richChatServer.model.Member;

/**
 *
 * @author Rémi Venant
 */
public interface AccountService {

    /**
     * Retrieve member instance based on its internal userId.
     *
     * @param userId the user id
     * @return the user's account
     * @throws AccessDeniedException if authorization fails
     * @throws ConstraintViolationException userId null, empty or invalid length
     * (24)
     * @throws NoSuchElementException the userId does not match any account
     */
    @PreAuthorize("hasRole('ADMIN') or authentication.name == #userId")
    Member getAccount(
            @NotNull @Pattern(regexp = "[abcdef0-9]{24}", flags = Pattern.Flag.CASE_INSENSITIVE) String userId
    ) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException;

    /**
     * Create a new account
     *
     * @param email user's email
     * @param username user's email
     * @param clearPassword member's password
     * @return the created member (with its internal id, but without the
     * credential)
     * @throws AccessDeniedException if authorization fails
     * @throws ConstraintViolationException invalid username, email or password
     * @throws DuplicateKeyException if the email used for the acount is already
     * known
     */
    @PreAuthorize("permitAll")
    Member createAccount(
            @NotNull @Email String email,
            @NotBlank String username,
            @NotBlank @Size(min = 4, max = 150) String clearPassword
    ) throws AccessDeniedException, ConstraintViolationException, DuplicateKeyException;

}
