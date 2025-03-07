package server.handler;

import model.response.result.ServiceException;
import spark.Request;

public abstract class RequestDeserializer<T, U>  extends ObjectSerializer<U> {
    @Override
    protected U serviceHandle(Request request) throws ServiceException {
        T serviceRequest = gson.fromJson(request.body(), getRequestClass());
        return serviceCall(serviceRequest, getAuthToken(request));
    }

    protected abstract U serviceCall(T serviceRequest, String authToken) throws ServiceException;

    protected abstract Class<T> getRequestClass();
}
