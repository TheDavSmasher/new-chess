package service;

import dataaccess.*;
import model.response.EmptyResponse;
import model.response.result.ServiceException;

public class AppService extends Service {
    public static EmptyResponse clearData() throws ServiceException {
        return tryCatch(() -> {
            AuthDAO.getInstance().clear();
            UserDAO.getInstance().clear();
            GameDAO.getInstance().clear();
            return new EmptyResponse();
        });
    }
}
