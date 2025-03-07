package service;

import dataaccess.*;
import model.dataaccess.AuthData;
import model.request.UserEnterRequest;
import model.response.EmptyResponse;
import model.response.UserEnterResponse;
import model.response.result.*;

public class UserService extends Service {
    public static UserEnterResponse register(UserEnterRequest request) throws ServiceException {
        return tryCatch(() -> {
            if (request.username() == null || request.password() == null || request.email() == null ||
                    request.username().isEmpty() || request.password().isEmpty() || request.email().isEmpty()) {
                throw new BadRequestException();
            }

            UserDAO userDAO = UserDAO.getInstance();

            if (userDAO.getUser(request.username()) != null) {
                throw new PreexistingException();
            }
            userDAO.createUser(request.username(), request.password(), request.email());
            return login(request);
        });
    }

    public static UserEnterResponse login(UserEnterRequest request) throws ServiceException {
        return tryCatch(() -> {
            if (request.username() == null || request.password() == null || request.username().isEmpty() || request.password().isEmpty()) {
                throw new BadRequestException();
            }

            UserDAO userDAO = UserDAO.getInstance();
            AuthDAO authDAO = AuthDAO.getInstance();

            if (userDAO.getUser(request.username(), request.password()) == null) {
                throw new UnauthorizedException();
            }
            AuthData newAuth = authDAO.createAuth(request.username());
            return new UserEnterResponse(newAuth.username(), newAuth.authToken());
        });
    }

    public static EmptyResponse logout(String authToken) throws ServiceException {
        return tryCatch(() -> {
            validateAuth(authToken);
            AuthDAO authDAO = AuthDAO.getInstance();
            authDAO.deleteAuth(authToken);
            return new EmptyResponse();
        });
    }

    public static AuthData getUser(String authToken) throws ServiceException {
        return tryCatch(() -> {
            AuthDAO authDAO = AuthDAO.getInstance();
            return authDAO.getAuth(authToken);
        });
    }

    public static AuthData validateAuth(String authToken) throws ServiceException {
        AuthData userAuth = getUser(authToken);
        if (userAuth == null) {
            throw new UnauthorizedException();
        }
        return userAuth;
    }
}
