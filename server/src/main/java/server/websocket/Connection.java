package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public record Connection(String username, Session session) {
    public void send(String message) {
        try {
            session.getRemote().sendString(message);
        } catch (IOException ignored) {}
    }

    public boolean isOpen() {
        return session.isOpen();
    }
}
