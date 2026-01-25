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
import java.util.NoSuchElementException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import richChatServer.model.Member;
import richChatServer.model.MemberCredential;
import richChatServer.model.MemberCredentialRepository;
import richChatServer.model.MemberRepository;

/**
 *
 * @author Rémi Venant
 */
@Service
@Validated
public class AccountServiceImpl implements AccountService {

    private static final Log LOG = LogFactory.getLog(AccountServiceImpl.class);

    private final MemberCredentialRepository memberCredRepo;

    private final MemberRepository memberRepo;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AccountServiceImpl(MemberCredentialRepository memberCredRepo, MemberRepository memberRepo, PasswordEncoder passwordEncoder) {
        this.memberCredRepo = memberCredRepo;
        this.memberRepo = memberRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Member getAccount(String userId) throws AccessDeniedException, ConstraintViolationException, NoSuchElementException {
        /*
        Precondition:
        - userId is validated
         */
        return this.memberRepo.findById(userId).get();
    }

    @Override
    public Member createAccount(String email, String username, String clearPassword) throws AccessDeniedException, ConstraintViolationException, DuplicateKeyException {
        /*
        Preconditions:
        - email, username and clearPassword validated
         */
        // Attempt password encoding
        final String encodedPassword = this.passwordEncoder.encode(clearPassword);
        // Create a proper account to ensure properties are properly managed
        Member member = new Member(email, username);
        // Create the account. May raise DuplicateKeyException or ConstraintViolationException
        member = this.memberRepo.save(member);
        try {
            // Create the member's credential (should not raise DuplicateKeyException if previous instruction passed)
            this.memberCredRepo.save(new MemberCredential(member, encodedPassword));
        } catch (Throwable ex) {
            try {
                this.memberRepo.delete(member);
            } catch (Throwable ex2) {
                LOG.warn("An error happened while saving member cred and cannot removed saved user");
            }
            throw ex;
        }

        return member;
    }

}
