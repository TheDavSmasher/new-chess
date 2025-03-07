package server.handler;

import model.request.JoinGameRequest;
import model.response.EmptyResponse;
import model.response.result.ServiceException;
import service.GameService;

public class JoinGameHandler extends RequestDeserializer<JoinGameRequest, EmptyResponse> {
    @Override
    protected EmptyResponse serviceCall(JoinGameRequest joinGameRequest, String authToken) throws ServiceException {
        return GameService.joinGame(joinGameRequest, authToken);
    }

    @Override
    protected Class<JoinGameRequest> getRequestClass() {
        return JoinGameRequest.class;
    }
}
