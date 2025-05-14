package server.handler;

import model.request.UserEnterRequest;
import model.response.UserEnterResponse;
import model.response.result.ServiceException;
import service.UserService;

public class LoginHandler extends UserEnterHandler {
    @Override
    protected UserEnterResponse serviceCall(UserEnterRequest userEnterRequest, String ignored) throws ServiceException {
        return UserService.login(userEnterRequest);
    }
}
