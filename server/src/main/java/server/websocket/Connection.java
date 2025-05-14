package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

public record Connection(String username, Session session) {
    public void send(Object message) {
        WSServer.send(session, message);
    }

    public boolean isOpen() {
        return session.isOpen();
    }
}
