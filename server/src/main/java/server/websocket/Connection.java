package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;

public class Connection {
    public final String username;
    public final Session session;

    public Connection(String name, Session newSession) {
        username = name;
        session = newSession;
    }

    public void send(String message) throws IOException {
        session.getRemote().sendString(message);
    }
}
