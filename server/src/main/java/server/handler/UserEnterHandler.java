package server.handler;

import model.request.UserEnterRequest;
import model.response.UserEnterResponse;

public abstract class UserEnterHandler extends RequestDeserializer<UserEnterRequest, UserEnterResponse> {
    @Override
    protected Class<UserEnterRequest> getRequestClass() {
        return UserEnterRequest.class;
    }
}
