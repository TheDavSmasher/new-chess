package websocket.serverMessages;

public class ErrorMessage extends ServerMessage {
    private final String errorMessage;
    public ErrorMessage(String errorMessage) {
        super(ServerMessageType.ERROR);
        this.errorMessage = errorMessage;
    }

    public String getError() {
        return errorMessage;
    }
}
