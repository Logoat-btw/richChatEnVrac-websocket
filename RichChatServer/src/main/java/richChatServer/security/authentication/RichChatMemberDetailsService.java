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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import richChatServer.model.Member;
import richChatServer.model.MemberCredential;

/**
 * Service de récupération d'un membre et de ses credentials d'après son email
 *
 * @author Rémi Venant
 */
public class RichChatMemberDetailsService implements UserDetailsService {

    private static final Log LOG = LogFactory.getLog(RichChatMemberDetailsService.class);

    private final MongoTemplate mongoTemplate;

    private final String localAdminMail;

    public RichChatMemberDetailsService(MongoTemplate mongoTemplate, String localAdminMail) {
        this.mongoTemplate = mongoTemplate;
        this.localAdminMail = localAdminMail;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Fetch member in db
        final Member member = this.findMemberByEmail(email);
        if (member != null) {
            // if member found fetch memberCred
            final MemberCredential cred = this.findMemberCredential(member);
            if (cred != null) {
                // Check if username matches the admin username
                final boolean isAdmin = this.localAdminMail != null & this.localAdminMail.equals(member.getEmail());
                // if member cred found, create user details.
                return new RichChatMemberDetails(member, cred, isAdmin);
            }
        }
        throw new UsernameNotFoundException("User not found or no credential");
    }

    private Member findMemberByEmail(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        try {
            // Get member by email, may return null if not found
            return this.mongoTemplate
                    .findOne(Query.query(Criteria.where("email").is(email)), Member.class);
        } catch (Exception ex) {
            LOG.error("Error fetching member with mail \"" + email + "\"", ex);
            return null;
        }
    }

    private MemberCredential findMemberCredential(Member member) {
        if (member == null || member.getId() == null) {
            return null;
        }
        try {
            // Get memberCred by member. May return null
            return this.mongoTemplate
                    .findOne(Query.query(Criteria.where("member").is(member)), MemberCredential.class);
        } catch (Exception ex) {
            LOG.error("Error fetching memberCred with member \"" + member.getEmail() + "\"", ex);
            return null;
        }
    }
}
