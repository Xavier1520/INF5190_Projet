package com.inf5190.chat.auth;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.inf5190.chat.auth.model.LoginRequest;
import com.inf5190.chat.auth.model.LoginResponse;
import com.inf5190.chat.auth.repository.FirestoreUserAccount;
import com.inf5190.chat.auth.repository.UserAccountRepository;
import com.inf5190.chat.auth.session.SessionData;
import com.inf5190.chat.auth.session.SessionDataAccessor;
import com.inf5190.chat.auth.session.SessionManager;

@RestController()
public class AuthController {

    private final SessionManager sessionManager;
    private final SessionDataAccessor sessionDataAccessor;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(
        SessionManager sessionManager,
        SessionDataAccessor sessionDataAccessor,
        UserAccountRepository userAccountRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.sessionManager = sessionManager;
        this.sessionDataAccessor = sessionDataAccessor;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("auth/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        try {
            FirestoreUserAccount account = userAccountRepository.getUserAccount(loginRequest.username());
            if (account != null) {
                String storedPassword = account.getEncodedPassword();
                if (!passwordEncoder.matches(loginRequest.password(), storedPassword))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            } else {    
                String encodedPassword = passwordEncoder.encode(loginRequest.password());
                account = new FirestoreUserAccount(loginRequest.username(), encodedPassword);
                userAccountRepository.setUserAccount(account);
            }
            final String token = this.sessionManager.addSession(new SessionData(loginRequest.username()));
            return new LoginResponse(token);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error on post message.");
        }
    }

    @PostMapping("auth/logout")
    public void logout(HttpServletRequest request) {
        final String token = this.sessionDataAccessor.getToken(request);
        this.sessionManager.removeSession(token);
        return;
    }
}
