package server.handler;

import model.request.UserEnterRequest;
import model.response.UserEnterResponse;
import model.response.result.ServiceException;

public abstract class UserEnterHandler extends RequestDeserializer<UserEnterRequest, UserEnterResponse> {
    @Override
    protected UserEnterResponse serviceCall(UserEnterRequest userEnterRequest, String ignored) throws ServiceException {
        return handleEnter(userEnterRequest);
    }

    protected abstract UserEnterResponse handleEnter(UserEnterRequest userEnterRequest) throws ServiceException;

    @Override
    protected Class<UserEnterRequest> getRequestClass() {
        return UserEnterRequest.class;
    }
}
