import java.io.*;
import java.net.*;

public class Requete implements Runnable {
    private Socket clientSocket;
    private String serverBaseURL;
    private CacheManager cacheManager;

    public Requete(Socket socket, String serverBaseURL, CacheManager cacheManager) {
        this.clientSocket = socket;
        this.serverBaseURL = serverBaseURL;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run() {
        String clientId = clientSocket.getInetAddress().toString();

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String requestLine = in.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            ProxyServer.log("[" + getCurrentTime() + "] Requête reçue de " + clientId + ": " + requestLine);

            String[] requestParts = requestLine.split(" ");
            if (requestParts.length < 3 || !requestParts[0].equals("GET")) {
                out.println("HTTP/1.1 400 Bad Request");
                out.println("Content-Type: text/plain");
                out.println();
                out.println("Bad Request: Invalid HTTP method or format.");

                ProxyServer.log("[" + getCurrentTime() + "] 400 Bad Request pour " + clientId);
                return;
            }

            String requestedPath = requestParts[1];
            if (requestedPath.equals("/")) {
                requestedPath = "/index.php";
            }

            if (requestedPath.equals("/listCaches")) {
                String cacheList = cacheManager.listCache(clientId);
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/plain");
                out.println("Connection: keep-alive");
                out.println();
                out.println(cacheList);

                ProxyServer.log("[" + getCurrentTime() + "] Réponse envoyée à " + clientId + ": Liste des caches");
                return;
            }
            if (requestedPath.startsWith("/clear:")) {
                if (requestedPath.equals("/clear:all")) {
                    cacheManager.clearAll(clientId);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println("Connection: keep-alive");
                    out.println();
                    out.println("All caches cleared for your session.");

                    ProxyServer.log("[" + getCurrentTime() + "] Réponse envoyée à " + clientId + ": Cache effacé pour toute la session");
                } else {
                    String fileName = requestedPath.substring(7);
                    if (!fileName.startsWith("/")) {
                        fileName = "/" + fileName;
                    }
                    boolean removed = cacheManager.remove(clientId, fileName);
                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: text/plain");
                    out.println("Connection: keep-alive");
                    out.println();
                    if (removed) {
                        out.println("Cache cleared for: " + fileName);

                        ProxyServer.log("[" + getCurrentTime() + "] Réponse envoyée à " + clientId + ": Cache effacé pour " + fileName);
                    } else {
                        out.println("No cache entry found for: " + fileName);
                        ProxyServer.log("[" + getCurrentTime() + "] Réponse envoyée à " + clientId + ": Aucun cache trouvé pour " + fileName);
                    }
                }
                return;
            }

            String cachedResponse = cacheManager.get(clientId, requestedPath);
            if (cachedResponse != null) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: text/html");
                out.println("Connection: keep-alive");
                out.println();
                out.println("Cache Hit");
                out.println(cachedResponse);

                ProxyServer.log("[" + getCurrentTime() + "] Cache Hit pour " + clientId + ": " + requestedPath);
            } else {
                // Si la réponse n'est pas en cache, faire une requête au serveur principal
                URL url = new URL(serverBaseURL + requestedPath);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                try {
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        try (BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                            StringBuilder response = new StringBuilder();
                            String line;

                            while ((line = responseReader.readLine()) != null) {
                                response.append(line).append("\n");
                            }

                            String responseBody = response.toString();
                            cacheManager.put(clientId, requestedPath, responseBody);

                            out.println("HTTP/1.1 200 OK");
                            out.println("Content-Type: text/html");
                            out.println("Connection: keep-alive");
                            out.println();
                            out.println("Cache Miss");
                            out.println(responseBody);

                            ProxyServer.log("[" + getCurrentTime() + "] Cache Miss pour " + clientId + ": " + requestedPath);
                        }
                    } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                        out.println("HTTP/1.1 404 Not Found");
                        out.println("Content-Type: text/plain");
                        out.println("Connection: keep-alive");
                        out.println();
                        out.println("Error 404: The requested resource was not found on the server.");

                        ProxyServer.log("[" + getCurrentTime() + "] 404 Not Found pour " + clientId + ": " + requestedPath);
                    } else {
                        out.println("HTTP/1.1 " + responseCode + " " + connection.getResponseMessage());
                        out.println("Content-Type: text/plain");
                        out.println("Connection: keep-alive");
                        out.println();
                        out.println("An unexpected error occurred.");

                        ProxyServer.log("[" + getCurrentTime() + "] Erreur inattendue pour " + clientId + ": " + responseCode);
                    }
                } catch (IOException e) {
                    out.println("HTTP/1.1 500 Internal Server Error");
                    out.println("Content-Type: text/plain");
                    out.println("Connection: keep-alive");
                    out.println();
                    out.println("Internal Server Error: Could not retrieve the requested resource.");
                    ProxyServer.log("[" + getCurrentTime() + "] 500 Internal Server Error pour " + clientId);
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors du traitement de la requête : " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Erreur lors de la fermeture de la connexion : " + e.getMessage());
            }
        }
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date());
    }
}
