package server.handler;

import static model.Serializer.*;
import model.response.result.BadRequestException;
import model.response.result.PreexistingException;
import model.response.result.ServiceException;
import model.response.result.UnauthorizedException;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public abstract class ObjectSerializer<T> implements Route {
    public String handle(Request request, Response response) {
        response.type("application/json");
        response.status(200);
        try {
            T serviceResponse = serviceHandle(request);
            return serialize(serviceResponse);
        } catch (ServiceException e) {
            int statusCode = switch (e) {
                case BadRequestException ignore -> 400;
                case UnauthorizedException ignore -> 401;
                case PreexistingException ignore -> 403;
                default -> 500;
            };
            throw Spark.halt(statusCode, "{ \"message\": \"Error: " + e.getMessage() + "\" }");
        }
    }

    protected abstract T serviceHandle(Request request) throws ServiceException;

    protected static String getAuthToken(Request request) {
        return request.headers("authorization");
    }
}
