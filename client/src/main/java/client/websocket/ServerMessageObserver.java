package client.websocket;

import websocket.serverMessages.ServerMessage;

public interface ServerMessageObserver {
    void notify(ServerMessage message);
}
