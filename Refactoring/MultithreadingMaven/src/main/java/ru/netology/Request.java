package ru.netology;

import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private String path;
    private Map<String, List<String>> queryParams;

    public Request(String requestLine) {
        final var parts = requestLine.split(" ");

        String fullPath = parts.length > 1 ? parts[1] : "";

        try {
            URI uri = new URI(fullPath);
            this.path = uri.getPath();
            this.queryParams = parseQueryParams(uri.getQuery());
        } catch (URISyntaxException e) {
            int queryStart = fullPath.indexOf('?');
            if (queryStart >= 0) {
                this.path = fullPath.substring(0, queryStart);
                this.queryParams = parseQueryParams(fullPath.substring(queryStart + 1));
            } else {
                this.path = fullPath;
                this.queryParams = new HashMap<>();
            }
        }
    }

    private Map<String, List<String>> parseQueryParams(String query) {
        Map<String, List<String>> params = new HashMap<>();

        if (query == null || query.isEmpty()) {
            return params;
        }

        List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        for (NameValuePair pair : pairs) {
            params.computeIfAbsent(pair.getName(), k -> new ArrayList<>())
                    .add(pair.getValue());
        }

        return params;
    }

    public String getPath() {
        return path;
    }

    public List<String> getQueryParam(String name) {
        return queryParams.getOrDefault(name, Collections.emptyList());
    }

    public Map<String, List<String>> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
}
