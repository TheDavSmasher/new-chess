package model.response.result;

public class BadRequestException extends ServiceException {
    public BadRequestException() {
        super("bad request");
    }
}
