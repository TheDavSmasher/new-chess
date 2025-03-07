package websocket.serverMessages;

public class Notification extends ServerMessage {
    private final String message;

    public Notification(String message) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
    }

    public String getNotification() {
        return message;
    }
}
