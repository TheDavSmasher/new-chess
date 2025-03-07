package service;

import model.dataaccess.AuthData;
import model.request.UserEnterRequest;
import model.response.UserEnterResponse;
import model.response.result.BadRequestException;
import model.response.result.PreexistingException;
import model.response.result.ServiceException;
import model.response.result.UnauthorizedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    String username = "davhig22";
    String password = "pass123";
    String email = "davhig22@byu.edu";
    String wrongUsername = "dabhig23";
    String wrongPassword = "shall-not-pass";
    UserEnterRequest enterRequest = new UserEnterRequest(username, password, email);
    String authToken;

    @BeforeEach
    public void setUp() throws ServiceException {
        AppService.clearData();
    }

    @Test
    public void registerTest() throws ServiceException {
        UserEnterResponse response = UserService.register(enterRequest);
        Assertions.assertEquals(username, response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    public void registerFail() throws ServiceException {
        UserEnterRequest badUsername = new UserEnterRequest("", password, email);
        Assertions.assertThrows(BadRequestException.class, () -> UserService.register(badUsername));

        UserEnterRequest badPassword = new UserEnterRequest(username, "", email);
        Assertions.assertThrows(BadRequestException.class, () -> UserService.register(badPassword));

        UserEnterRequest badEmail = new UserEnterRequest(username, password, "");
        Assertions.assertThrows(BadRequestException.class, () -> UserService.register(badEmail));

        UserService.register(enterRequest);
        Assertions.assertThrows(PreexistingException.class, () -> UserService.register(enterRequest));
    }

    @Test
    public void loginTest() throws ServiceException {
        UserService.register(enterRequest);
        Assertions.assertDoesNotThrow(() -> UserService.login(enterRequest));

        UserEnterResponse response = UserService.login(enterRequest);
        Assertions.assertEquals(username, response.username());
        Assertions.assertNotNull(response.authToken());
    }

    @Test
    public void loginFail() throws ServiceException {
        Assertions.assertThrows(UnauthorizedException.class, () -> UserService.login(enterRequest));

        UserService.register(enterRequest);

        UserEnterRequest wrongUser = new UserEnterRequest(wrongUsername, password, null);
        Assertions.assertThrows(UnauthorizedException.class, () -> UserService.login(wrongUser));

        UserEnterRequest wrongPass = new UserEnterRequest(username, wrongPassword, null);
        Assertions.assertThrows(UnauthorizedException.class, () -> UserService.login(wrongPass));
    }

    @Test
    public void logoutTest() throws ServiceException {
        authToken = UserService.register(enterRequest).authToken();
        Assertions.assertDoesNotThrow(() -> UserService.logout(authToken));

        authToken = UserService.login(enterRequest).authToken();
        Assertions.assertDoesNotThrow(() -> UserService.logout(authToken));
    }

    @Test
    public void logoutFail() throws ServiceException {
        authToken = "not-an-auth-token";
        Assertions.assertThrows(UnauthorizedException.class, () -> UserService.logout(authToken));

        authToken = UserService.register(enterRequest).authToken();
        UserService.logout(authToken);
        Assertions.assertThrows(UnauthorizedException.class, () -> UserService.logout(authToken));
    }

    @Test
    public void validUserTest() throws ServiceException {
        String authToken = "non-existent";
        AuthData auth = UserService.getUser(authToken);

        Assertions.assertNull(auth);

        authToken = UserService.register(enterRequest).authToken();
        auth = UserService.getUser(authToken);

        Assertions.assertNotNull(auth);
        Assertions.assertEquals(username, auth.username());
        Assertions.assertNotNull(auth.authToken());
    }
}