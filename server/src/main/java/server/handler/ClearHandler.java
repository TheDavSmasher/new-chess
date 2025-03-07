package server.handler;

import model.response.EmptyResponse;
import model.response.result.ServiceException;
import service.AppService;
import spark.Request;

public class ClearHandler extends ObjectSerializer<EmptyResponse> {
    @Override
    public EmptyResponse serviceHandle(Request ignored) throws ServiceException {
        return AppService.clearData();
    }
}
