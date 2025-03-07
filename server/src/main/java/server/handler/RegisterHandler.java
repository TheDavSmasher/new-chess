package server.handler;

import model.request.UserEnterRequest;
import model.response.UserEnterResponse;
import model.response.result.ServiceException;
import service.UserService;

public class RegisterHandler extends UserEnterHandler {
    @Override
    protected UserEnterResponse handleEnter(UserEnterRequest userEnterRequest) throws ServiceException {
        return UserService.register(userEnterRequest);
    }
}
