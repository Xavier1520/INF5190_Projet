package com.inf5190.chat.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import com.inf5190.chat.auth.model.LoginRequest;
import com.inf5190.chat.auth.model.LoginResponse;
import com.inf5190.chat.auth.repository.FirestoreUserAccount;
import com.inf5190.chat.auth.repository.UserAccountRepository;
import com.inf5190.chat.auth.session.SessionData;
import com.inf5190.chat.auth.session.SessionDataAccessor;
import com.inf5190.chat.auth.session.SessionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;

public class TestAuthController {
    private final String username = "username";
    private final String password = "pwd";
    private final String hashedPassword = "hash";
    private final FirestoreUserAccount userAccount = new FirestoreUserAccount(this.username, this.hashedPassword);
    private final LoginRequest loginRequest = new LoginRequest(this.username, this.password);
    
    private final String incorrectPassword = "incorrect";
    private final LoginRequest incorrectRequest = new LoginRequest(this.username, this.incorrectPassword);

    private final String newUsername = "newusername";
    private final String newPassword = "newpwd";
    private final String newHashedPassword = "newhash";
    private final LoginRequest newRequest = new LoginRequest(this.newUsername, this.newPassword);

    @Mock
    private SessionManager mockSessionManager;

    @Mock
    private UserAccountRepository mockAccountRepository;

    @Mock
    private SessionDataAccessor mockSessionDataAccessor;

    @Mock
    private PasswordEncoder mockPasswordEncoder;

    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.authController = new AuthController(mockSessionManager, mockSessionDataAccessor, mockAccountRepository,
                mockPasswordEncoder);
    }

    @Test
    public void loginExistingUserAccountWithCorrectPassword() throws InterruptedException, ExecutionException {
        final SessionData expectedSessionData = new SessionData(this.username);
        final String expectedToken = "token";

        when(this.mockAccountRepository.getUserAccount(loginRequest.username())).thenReturn(userAccount);
        when(this.mockPasswordEncoder.matches(loginRequest.password(), this.hashedPassword)).thenReturn(true);
        when(this.mockSessionManager.addSession(expectedSessionData)).thenReturn(expectedToken);

        LoginResponse response = this.authController.login(loginRequest);
        assertThat(response.token()).isEqualTo(expectedToken);

        verify(this.mockAccountRepository, times(1)).getUserAccount(this.username);
        verify(this.mockPasswordEncoder, times(1)).matches(this.password, this.hashedPassword);
        verify(this.mockSessionManager, times(1)).addSession(expectedSessionData);
    }

    @Test
    public void loginExistingUserAccountWithIncorrectPassword() throws InterruptedException, ExecutionException {
        final HttpStatus expectedStatus = HttpStatus.FORBIDDEN;

        when(this.mockAccountRepository.getUserAccount(incorrectRequest.username())).thenReturn(userAccount);
        when(this.mockPasswordEncoder.matches(incorrectRequest.password(), this.hashedPassword)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            this.authController.login(incorrectRequest);
        });
        
        HttpStatus status = exception.getStatus();
        assertThat(status).isEqualTo(expectedStatus);

        verify(this.mockAccountRepository, times(1)).getUserAccount(this.username);
        verify(this.mockPasswordEncoder, times(1)).matches(this.incorrectPassword, this.hashedPassword);
    }

    @Test
    public void loginNewUserAccount() throws InterruptedException, ExecutionException {
        final SessionData expectedSessionData = new SessionData(this.newUsername);
        final String expectedToken = "token";

        when(this.mockAccountRepository.getUserAccount(newRequest.username())).thenReturn(null);
        when(this.mockPasswordEncoder.encode(newRequest.password())).thenReturn(newHashedPassword);
        when(this.mockSessionManager.addSession(expectedSessionData)).thenReturn(expectedToken);

        LoginResponse response = this.authController.login(newRequest);
        assertThat(response.token()).isEqualTo(expectedToken);

        verify(this.mockAccountRepository, times(1)).getUserAccount(this.newUsername);
        verify(this.mockPasswordEncoder, times(1)).encode(this.newPassword);
        verify(this.mockSessionManager, times(1)).addSession(expectedSessionData);
    }
}