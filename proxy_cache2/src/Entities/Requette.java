package src.Entities;
import java.util.HashMap;
import java.util.Map;

public class Requette {
    private String method;
    private String resource;
    private String version;
    private Map<String, String> headers;

    public Requette(String method, String resource, String version) {
        this.method = method;
        this.resource = resource;
        this.version = version;
        headers = new HashMap<>();
    }

    public Requette(String requette) {
        if (requette != null) {
            String[] parseQuery = requette.split(" ");
            this.method = parseQuery[0].trim();
            this.resource = parseQuery[1].replaceFirst("/", "");
            this.version = parseQuery[2];
            headers = new HashMap<>();
        }
    }

    public String getMethod() {
        return method;
    }

    public String getResource() {
        return resource;
    }

    public String getVersion() {
        return version;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(String header) {
        if (!header.isEmpty() && header.contains(":") && header != null) {
            String[] splited = header.split(":");
            headers.put(splited[0], splited[1]);
        }
    }

    public String getRequest() {
        StringBuilder response = new StringBuilder();
        response.append(method + " /" + resource + " " + version + "\r\n");
        headers.forEach((key, value) -> {
            response.append(key + ": " + value + "\r\n");
        });
        return response.toString();
    }

    public String getBasicRequest(String hostname) {
        StringBuilder response = new StringBuilder();
        response.append(method + " /" + resource + " " + version + "\r\nHost: " + hostname + "\r\n"
                + "Connection: close\r\n\r\n");
        return response.toString();
    }

}
