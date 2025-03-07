package client;

import com.google.gson.Gson;
import model.response.ErrorResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;

public class HttpCommunicator {
    public static <T> T doPost(String urlString, String body, String authToken, Class<T> responseClass) throws IOException {
        return doServerMethod(urlString, "POST", body, authToken, responseClass);
    }

    public static <T> void doDelete(String urlString, String authToken, Class<T> responseClass) throws IOException {
        doServerMethod(urlString, "DELETE", null, authToken, responseClass);
    }

    public static <T> void doPut(String urlString, String body, String authToken, Class<T> responseClass) throws IOException {
        doServerMethod(urlString, "PUT", body, authToken, responseClass);
    }

    public static <T> T doGet(String urlString, String authToken, Class<T> responseClass) throws IOException {
        return doServerMethod(urlString, "GET", null, authToken, responseClass);
    }

    private static <T> T doServerMethod(String url, String method, String body, String authToken, Class<T> responseClass) throws IOException {
        HttpURLConnection connection = getConnection(url);

        connection.setConnectTimeout(5000);
        connection.setRequestMethod(method);
        if (authToken != null) {
            connection.addRequestProperty("Authorization", authToken);
        }
        if (body != null) {
            connection.setDoOutput(true);
            try (OutputStream requestBody = connection.getOutputStream()) {
                requestBody.write(body.getBytes());
            }
        }

        connection.connect();

        InputStreamReader reader;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (InputStream responseBody = connection.getInputStream()) {
                reader = new InputStreamReader(responseBody);
                return new Gson().fromJson(reader, responseClass);
            }
        } else {
            try (InputStream responseBody = connection.getErrorStream()) {
                reader = new InputStreamReader(responseBody);
                BufferedReader bufferedReader = new BufferedReader(reader);
                ErrorResponse errorMessage = new Gson().fromJson(bufferedReader.readLine(), ErrorResponse.class);
                throw new IOException(errorMessage.message());
            }
        }
    }

    private static HttpURLConnection getConnection(String urlString) throws IOException {
        return (HttpURLConnection) URI.create(urlString).toURL().openConnection();
    }
}
