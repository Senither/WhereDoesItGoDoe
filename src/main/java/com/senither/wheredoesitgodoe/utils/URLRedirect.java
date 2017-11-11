package com.senither.wheredoesitgodoe.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class URLRedirect {

    public static List<String> get(String url) throws IOException {
        return fetch(url, new ArrayList<>());
    }

    private static List<String> fetch(String url, List<String> redirects) throws IOException {
        redirects.add(url);

        HttpURLConnection connection = openHttpConnectionAndConnectFor(url);
        if (connection.getHeaderField("Location") == null) {
            return redirects;
        }
        return fetch(connection.getHeaderField("Location"), redirects);
    }

    private static HttpURLConnection openHttpConnectionAndConnectFor(String url) throws IOException {
        HttpURLConnection connection = prepareHttpConnection((HttpURLConnection) (new URL(url).openConnection()));

        connection.connect();

        return connection;
    }

    private static HttpURLConnection prepareHttpConnection(HttpURLConnection connection) throws ProtocolException {
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Cache-Control", "max-age=0");
        connection.setInstanceFollowRedirects(false);

        return connection;
    }
}
