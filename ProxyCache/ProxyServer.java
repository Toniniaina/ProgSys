import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyServer {
    private static JTextArea logArea;
    private static JFrame frame;
    private static Thread serverThread;
    private static ServerSocket serverSocket;
    private static AtomicBoolean isRunning = new AtomicBoolean(false);
    private static final Map<String, String> connectedClients = new ConcurrentHashMap<>();

    private static int cacheSize;
    private static long cacheTtl;
    private static CacheManager cacheManager;

    public static void main(String[] args) {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("\n>> ");
                String command = consoleReader.readLine().trim().toLowerCase();

                if (command.startsWith("clear:")) {
                    String[] commandParts = command.split(" ");
                    if (commandParts.length == 3) {
                        String cacheName = commandParts[1];
                        String ip = commandParts[2];
                        if (cacheName.equals("all")) {
                            clearAllCache(ip);
                        } else {
                            clearCache(cacheName, ip);
                        }
                    } else {
                        System.out.println("Commande incorrecte. Utilisation : clear: nomDuCache ip");
                    }
                } else {
                    switch (command) {
                        case "start":
                            if (!isRunning.get()) {
                                startServer();
                                log("[" + getCurrentTime() + "] Proxy Cache Server démarré.");
                            } else {
                                System.out.println("Le serveur est déjà en cours d'exécution.");
                            }
                            break;
                        case "stop":
                            if (isRunning.get()) {
                                stopServer();
                            } else {
                                System.out.println("Le serveur n'est pas en cours d'exécution.");
                            }
                            break;
                        case "restart":
                            if (isRunning.get()) {
                                stopServer();
                            }
                            startServer();
                            break;
                        case "status":
                            System.out.println(isRunning.get() ? "Le serveur est actif." : "Le serveur est arrêté.");
                            break;
                        case "ls clients":
                            listClients();
                            break;
                        case "ls caches":
                            listCaches();
                            break;
                        default:
                            System.out.println("Commande inconnue.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Erreur lors de la lecture de la commande : " + e.getMessage());
            }
        }
    }

    private static void clearCache(String cacheName, String ip) {
        if (connectedClients.containsKey(ip)) {
            if (cacheManager.remove(ip, cacheName)) {
                log("[" + getCurrentTime() + "] Cache " + cacheName + " supprimé pour le client " + ip);
            } else {
                log("[" + getCurrentTime() + "] Cache " + cacheName + " non trouvé pour le client " + ip);
            }
        } else {
            log("[" + getCurrentTime() + "] Client avec IP " + ip + " non trouvé.");
        }
    }

    private static void clearAllCache(String ip) {
        if (connectedClients.containsKey(ip)) {
            cacheManager.clearAll(ip);
            log("[" + getCurrentTime() + "] Tous les caches ont été supprimés pour le client " + ip);
        } else {
            log("[" + getCurrentTime() + "] Client avec IP " + ip + " non trouvé.");
        }
    }



    private static void startServer() {
        isRunning.set(true);
        System.out.println("Démarrage du serveur...");
        serverThread = new Thread(() -> {
            try {
                SwingUtilities.invokeLater(() -> createAndShowGUI());

                ConfigLoader config = ConfigLoader.getInstance("config.xml");
                int port = Integer.parseInt(config.getConfigValue("port"));
                String serverBaseUrl = config.getConfigValue("serverBaseURL");
                cacheSize = Integer.parseInt(config.getConfigValue("cacheSize"));
                cacheTtl = Long.parseLong(config.getConfigValue("cacheTTL"));

                cacheManager = new CacheManager(cacheSize, cacheTtl);
                serverSocket = new ServerSocket(port);

                SwingUtilities.invokeAndWait(() -> log("[" + getCurrentTime() + "] Proxy Cache Server démarré sur le port " + port));

                while (isRunning.get()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        String clientAddress = clientSocket.getInetAddress().toString();
                        String connectionTime = getCurrentTime();
                        connectedClients.put(clientAddress, connectionTime);

                        log("[" + connectionTime + "] Nouvelle connexion depuis " + clientAddress);

                        Thread thread = new Thread(new Requete(clientSocket, serverBaseUrl, cacheManager));
                        thread.start();
                    } catch (SocketException e) {
                        log("[" + getCurrentTime() + "] ServerSocket fermé.");
                    }
                }
            } catch (IOException e) {
                log("[" + getCurrentTime() + "] Erreur du serveur : " + e.getMessage());
            } catch (Exception e) {
                log("[" + getCurrentTime() + "] Erreur de configuration : " + e.getMessage());
            }
        });
        serverThread.start();
    }

    private static void stopServer() {
        isRunning.set(false);
        System.out.println("Arrêt du serveur...");
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (serverThread != null && serverThread.isAlive()) {
                serverThread.join();
            }
            connectedClients.clear();
        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur lors de l'arrêt du serveur : " + e.getMessage());
        }
        closeGUI();
    }

    private static void listClients() {
        if (connectedClients.isEmpty()) {
            System.out.println("Aucun client actif.");
        } else {
            System.out.println("Liste des clients actifs :");
            connectedClients.forEach((client, time) -> System.out.println("- " + client + " (connecté à " + time + ")"));
        }
    }

    private static void listCaches() {
        if (connectedClients.isEmpty()) {
            System.out.println("Aucun client actif.");
        } else {
            System.out.println("Caches stockés sur le serveur :");
            connectedClients.forEach((client, time) -> {
                String caches = cacheManager.listCache(client);
                System.out.println("Client : " + client);
                System.out.println(caches);
            });
        }
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Proxy Server Logs");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 500);

        logArea = new JTextArea();
        logArea.setEditable(false);

        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.WHITE);
        logArea.setFont(new Font("Consolas", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private static void closeGUI() {
        if (frame != null) {
            SwingUtilities.invokeLater(() -> frame.dispose());
        }
    }

    public static void log(String message) {
        if (logArea != null) {
            SwingUtilities.invokeLater(() -> {
                logArea.append(message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            });
        }
    }

    private static String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new java.util.Date());
    }
}
