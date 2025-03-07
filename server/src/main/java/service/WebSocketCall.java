package service;

import dataaccess.DataAccessException;
import model.response.result.ServiceException;

public interface WebSocketCall {
    void method() throws ServiceException, DataAccessException;
}
