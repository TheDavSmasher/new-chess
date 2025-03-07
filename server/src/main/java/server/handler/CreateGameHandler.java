package server.handler;

import model.request.CreateGameRequest;
import model.response.CreateGameResponse;
import model.response.result.ServiceException;
import service.GameService;

public class CreateGameHandler extends RequestDeserializer<CreateGameRequest, CreateGameResponse> {
    @Override
    protected CreateGameResponse serviceCall(CreateGameRequest createGameRequest, String authToken) throws ServiceException {
        return GameService.createGame(createGameRequest, authToken);
    }

    @Override
    protected Class<CreateGameRequest> getRequestClass() {
        return CreateGameRequest.class;
    }
}
