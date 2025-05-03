package server.handler;

import com.google.gson.Gson;
import model.response.result.BadRequestException;
import model.response.result.PreexistingException;
import model.response.result.ServiceException;
import model.response.result.UnauthorizedException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public abstract class ObjectSerializer<T> implements Route {
    protected final Gson gson = new Gson();

    public String handle(Request request, Response response) {
        response.type("application/json");
        response.status(200);
        try {
            T serviceResponse = serviceHandle(request);

            return gson.toJson(serviceResponse);
        } catch (ServiceException e) {
            throw Spark.halt(getStatusCode(e), "{ \"message\": \"Error: " + e.getMessage() + "\" }");
        }
    }

    protected abstract T serviceHandle(Request request) throws ServiceException;

    protected String getAuthToken(Request request) {
        return request.headers("authorization");
    }

    private int getStatusCode(ServiceException e) {
        return switch (e) {
            case BadRequestException ignore -> 400;
            case UnauthorizedException ignore -> 401;
            case PreexistingException ignore -> 403;
            default -> 500;
        };
    }
}
