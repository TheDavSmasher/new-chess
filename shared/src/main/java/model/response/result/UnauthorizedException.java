package model.response.result;

public class UnauthorizedException extends ServiceException {
    public UnauthorizedException() {
        super("unauthorized");
    }
}
