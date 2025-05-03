package model;

import com.google.gson.Gson;

public interface Serializer {
    Gson gson = new Gson();

    static <T> T deserialize(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    static String serialize(Object object) {
        return gson.toJson(object);
    }
}
