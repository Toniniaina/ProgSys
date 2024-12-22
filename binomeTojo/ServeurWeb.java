import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ServeurWeb {
    private static int PORT;
    private static String XAMPP_SERVER_URL;
    private static boolean CACHE_ENABLED;
    private static int SESSION_TIMEOUT;
    private static final HashMap<String, String> CACHE = new HashMap<>(); // Cache des ressources
    private static final HashMap<String, Session> PHP_SESSIONS = new HashMap<>(); // Stockage des sessions PHP

    static {
        try {
            loadConfig();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);  // Exit si le chargement de la config échoue
        }
    }
    public static void clearCache() {
        CACHE.clear();
        System.out.println("Cache vidé avec succès.");
    }

    public static void removeFromCache(String resourcePath) {
        if (CACHE.containsKey(resourcePath)) {
            CACHE.remove(resourcePath);
            System.out.println("Ressource supprimée du cache : " + resourcePath);
        }
    }

    public static List<String> getCacheList() {
        return new ArrayList<>(CACHE.keySet());  // Retourne une liste des clés du cache
    }
    private static void loadConfig() throws Exception {
        File configFile = new File("config.xml");
        if (!configFile.exists()) {
            throw new FileNotFoundException("Le fichier de configuration 'config.xml' est introuvable.");
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(configFile);
        document.getDocumentElement().normalize();

        // Charger les paramètres du serveur
        NodeList serverList = document.getElementsByTagName("server");
        if (serverList.getLength() > 0) {
            Element serverElement = (Element) serverList.item(0);
            PORT = Integer.parseInt(serverElement.getElementsByTagName("port").item(0).getTextContent());
            XAMPP_SERVER_URL = serverElement.getElementsByTagName("xamppUrl").item(0).getTextContent();
            CACHE_ENABLED = Boolean.parseBoolean(serverElement.getElementsByTagName("cacheEnabled").item(0).getTextContent());
            SESSION_TIMEOUT = Integer.parseInt(serverElement.getElementsByTagName("sessionTimeout").item(0).getTextContent());
        } else {
            throw new Exception("Configuration du serveur manquante dans le fichier XML.");
        }
    }

    private static void logToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("logs/server.log", true))) {
            writer.write(new Date() + " - " + message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Serveur démarré sur le port " + PORT);
        logToFile("Serveur démarré sur le port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            logToFile("Connexion entrante : " + clientSocket.getInetAddress());
            new Thread(new ClientHandler(clientSocket)).start();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String method;
        private String sessionId;
        private Map<String, String> requestHeaders = new HashMap<>();
        private Map<String, String> requestParams = new HashMap<>();

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                // Lire l'ensemble de la requête HTTP
                String requestLine = in.readLine();
                if (requestLine == null || requestLine.isEmpty()) {
                    return;
                }

                logToFile("Requête brute reçue : " + requestLine); // Ajouter ici un log de la requête brute

                // Parser la première ligne
                String[] requestParts = requestLine.split(" ");
                method = requestParts[0];
                String path = requestParts[1];
                logToFile("Requête reçue : Méthode " + method + " pour le chemin " + path);

                // Lire les en-têtes
                String headerLine;
                while ((headerLine = in.readLine()) != null && !headerLine.isEmpty()) {
                    String[] headerParts = headerLine.split(": ", 2);
                    if (headerParts.length == 2) {
                        requestHeaders.put(headerParts[0].toLowerCase(), headerParts[1]);
                        logToFile("En-tête : " + headerParts[0] + " : " + headerParts[1]); // Log des en-têtes
                    }
                }

                // Gestion des paramètres GET
                if (path.contains("?")) {
                    String[] pathAndQuery = path.split("\\?");
                    path = pathAndQuery[0];
                    parseQueryString(pathAndQuery[1]);
                    logToFile("Paramètres GET : " + requestParams.toString()); // Log des paramètres GET
                }

                // Gestion des paramètres POST
                if ("POST".equalsIgnoreCase(method)) {
                    int contentLength = Integer.parseInt(requestHeaders.getOrDefault("content-length", "0"));
                    if (contentLength > 0) {
                        char[] buffer = new char[contentLength];
                        in.read(buffer, 0, contentLength);
                        String postData = new String(buffer);
                        parseQueryString(postData);
                        logToFile("Paramètres POST : " + requestParams.toString()); // Log des paramètres POST
                    }
                }

                // Gestion des sessions PHP
                sessionId = getOrCreateSession();

                // Vérifier si la ressource est dans le cache
                if (CACHE_ENABLED && CACHE.containsKey(path)) {
                    logToFile("Ressource trouvée dans le cache pour " + path);
                    sendResponse("200 OK", "text/html; charset=UTF-8", CACHE.get(path), true);
                } else {
                    logToFile("Ressource non trouvée dans le cache, transmission à XAMPP pour " + path);
                    forwardRequestToXampp(path, sessionId);
                }

            } catch (IOException e) {
                e.printStackTrace();
                logToFile("Erreur dans le traitement de la requête : " + e.getMessage()); // Log de l'erreur
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void parseQueryString(String queryString) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    try {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8.toString());
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.toString());
                        requestParams.put(key, value);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private String getOrCreateSession() {
            String existingSessionId = requestHeaders.get("cookie");
            if (existingSessionId != null && PHP_SESSIONS.containsKey(existingSessionId)) {
                logToFile("Session existante trouvée : " + existingSessionId);
                // Vérifier l'expiration de la session
                Session session = PHP_SESSIONS.get(existingSessionId);
                if (session.isExpired(SESSION_TIMEOUT)) {
                    PHP_SESSIONS.remove(existingSessionId);
                    logToFile("Session expirée et supprimée.");
                    return createNewSession();
                }
                return existingSessionId;
            }
            return createNewSession();
        }

        private String createNewSession() {
            String newSessionId = "PHPSESSID=" + UUID.randomUUID().toString();
            Session newSession = new Session(newSessionId, System.currentTimeMillis());
            PHP_SESSIONS.put(newSessionId, newSession);
            logToFile("Nouvelle session créée : " + newSessionId);
            return newSessionId;
        }

        private void forwardRequestToXampp(String path, String sessionId) throws IOException {
            URL url = new URL(XAMPP_SERVER_URL + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty("Cookie", sessionId);

            if ("POST".equalsIgnoreCase(method) && !requestParams.isEmpty()) {
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                    StringBuilder postData = new StringBuilder();
                    for (Map.Entry<String, String> param : requestParams.entrySet()) {
                        if (postData.length() != 0) postData.append('&');
                        postData.append(URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8));
                        postData.append('=');
                        postData.append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
                    }
                    wr.writeBytes(postData.toString());
                }
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );
                StringBuilder responseContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    responseContent.append(line).append("\n");
                }
                reader.close();
                CACHE.put(path, responseContent.toString());
                sendResponse("200 OK", connection.getContentType(), responseContent.toString(), false);
            } else {
                sendError("404 Not Found", "Ressource PHP introuvable");
            }
        }

        private void sendResponse(String status, String contentType, String content, boolean fromCache) {
            out.println("HTTP/1.1 " + status);
            out.println("Content-Type: " + contentType);
            out.println();
            out.println(content);
            logToFile((fromCache ? "Cache" : "XAMPP") + " : Réponse envoyée pour " + contentType + " - " + status);
        }

        private void sendError(String status, String errorMessage) {
            out.println("HTTP/1.1 " + status);
            out.println("Content-Type: text/html; charset=UTF-8");
            out.println();
            out.println("<html><body><h1>" + status + "</h1><p>" + errorMessage + "</p></body></html>");
            logToFile("Erreur envoyée : " + status + " - " + errorMessage);
        }
    }

    // Classe pour gérer les sessions PHP
    static class Session {
        private String sessionId;
        private long lastAccessTime;

        public Session(String sessionId, long lastAccessTime) {
            this.sessionId = sessionId;
            this.lastAccessTime = lastAccessTime;
        }

        public boolean isExpired(int timeout) {
            return (System.currentTimeMillis() - lastAccessTime) > timeout;
        }
    }
}
