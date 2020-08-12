package org.sobotics.boson.framework.utils;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * AKA, TunaLib - All code is courtesy of Lord Tunaki
 */

public class HttpRequestUtils {
    public static JsonObject get(String url, String... data) throws IOException {
        Connection.Response response = Jsoup.connect(url).data(data).method(Connection.Method.GET).ignoreContentType(true)
                                            .ignoreHttpErrors(true).execute();
        String json = response.body();
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching URL " + url + ". Body is: " + response.body());
        }
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public static JsonObject post(String url, String... data) throws IOException {
        Connection.Response response = Jsoup.connect(url).data(data).method(Connection.Method.POST).ignoreContentType(true)
                                            .ignoreHttpErrors(true).execute();
        String json = response.body();
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching URL " + url + ". Body is: " + response.body());
        }
        return new JsonParser().parse(json).getAsJsonObject();
    }

    public static JsonObject postJson(String url, String data) throws IOException {
        Connection.Response response = Jsoup.connect(url).method(Connection.Method.POST).header("Content-Type", "application/json")
                                            .requestBody(data).ignoreContentType(true).ignoreHttpErrors(true).execute();
        String json = response.body();
        if (response.statusCode() != 200) {
            throw new IOException("HTTP " + response.statusCode() + " fetching URL " + url + ". Body is: " + response.body());
        }
        return new JsonParser().parse(json).getAsJsonObject();
    }
}
