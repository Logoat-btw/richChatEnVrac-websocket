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
package richChatServer.controllers.rest;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import richChatServer.model.Member;
import richChatServer.model.views.MemberViews;
import richChatServer.security.authentication.RichChatMemberDetails;
import richChatServer.services.AccountService;

/**
 *
 * @author Rémi Venant
 */
@RestController
@RequestMapping("/api/v1/rest/accounts")
public class AccountRestController {

    private final AccountService accountSvc;

    @Autowired
    public AccountRestController(AccountService accountService) {
        this.accountSvc = accountService;
    }

    @JsonView(MemberViews.Detailled.class)
    @GetMapping("myself")
    public ResponseEntity<Member> getMyself(@AuthenticationPrincipal RichChatMemberDetails currentUser) {
        if (currentUser == null) {
            return ResponseEntity.noContent().build();
        }
        Member member = this.accountSvc.getAccount(currentUser.getUsername());
        return ResponseEntity.ok(member);
    }

    @JsonView(MemberViews.Detailled.class)
    @PostMapping
    public Member createAccount(@RequestBody MemberCreation memberCreationInfo) {
        return this.accountSvc.createAccount(memberCreationInfo.email(), memberCreationInfo.username(), memberCreationInfo.password());
    }

    public static record MemberCreation(String email, String username, String password) {

    }
}
