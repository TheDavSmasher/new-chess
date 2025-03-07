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
        String result = null;
        try {
            T serviceResponse = serviceHandle(request);
            result = gson.toJson(serviceResponse);
        } catch (ServiceException e) {
            Spark.halt(getStatusCode(e), "{ \"message\": \"Error: " + e.getMessage() + "\" }");
        }
        response.status(200);
        return result;
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
