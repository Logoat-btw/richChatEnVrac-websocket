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

import java.io.Serializable;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import richChatServer.model.Member;
import richChatServer.model.MemberCredential;

/**
 *
 * @author Rémi Venant
 */
public class RichChatMemberDetails extends User implements Serializable {

    private final Member member;

    public RichChatMemberDetails(Member member, MemberCredential credential, boolean isAdmin) {
        super(member.getId(), credential.getEncodedPassword(), computeAuthoritiesFromMember(member, isAdmin));
        this.member = member;
    }

    public RichChatMemberDetails(Member member, MemberCredential credential, boolean isAdmin, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked) {
        super(member.getId(), credential.getEncodedPassword(), enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, computeAuthoritiesFromMember(member, isAdmin));
        this.member = member;
    }

    private static Collection<? extends GrantedAuthority> computeAuthoritiesFromMember(Member member, boolean isAdmin) {
        if (isAdmin) {
            return List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"));
        } else {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
    }

    public Member getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "RichChatMemberDetails{" + "email=" + member.getEmail() + ", username=" + this.getUsername() + '}';
    }

    public static RichChatMemberDetails getFromPrincipal(Principal principal) {
        switch (principal) {
            case null -> {
                return null;
            }
            case RichChatMemberDetails member -> {
                return member;
            }
            case UsernamePasswordAuthenticationToken uat -> {
                if (uat.getPrincipal() instanceof RichChatMemberDetails member) {
                    return member;
                } else {
                    return null;
                }
            }
            default -> {
                return null;
            }
        }
    }

}
