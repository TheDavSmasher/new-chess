package server.websocket.commands;

import model.response.result.ServiceException;
import org.eclipse.jetty.websocket.api.Session;
import server.websocket.ConnectionManager;
import service.GameService;
import websocket.commands.LeaveCommand;
import websocket.messages.Notification;

public class WSLeave extends WSChessCommand<LeaveCommand> {
    public WSLeave(ConnectionManager connectionManager) {
        super(connectionManager);
    }

    @Override
    protected Class<LeaveCommand> GetCommandClass() {
        return LeaveCommand.class;
    }

    @Override
    protected void Execute(LeaveCommand command, Session session) {
        try {
            String username = CheckConnection(command.getAuthToken());
            GameService.leaveGame(command.getAuthToken(), command.getGameID());
            connectionManager.removeFromGame(command.getGameID(), command.getAuthToken());
            Notification notification = new Notification(username + " has left the game.");
            connectionManager.notifyOthers(command.getGameID(), command.getAuthToken(), notification);
        } catch (ServiceException e) {
            sendError(session, e.getMessage());
        }
    }
}
