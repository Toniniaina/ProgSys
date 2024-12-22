package src.Serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.Cache.Cache;
import src.Cache.CacheManager;
import src.Entities.Requette;
import src.Entities.SettingMapper;

public class Serveur {
    private int serverPort = 3000;
    private String nextServer = "127.0.0.1";
    private int nextServerPort = 80;
    private ServerSocket serverSocket;
    private Cache localCache;
    private boolean active;
    private boolean promptActive;
    private CacheManager cacheGUI;
    private boolean running;
    private ClientManager clientManager;
    private int cacheDuration;
    private int cacheLength;
    private Scanner inputScanner;

    public ClientManager getClientManager() {
        return clientManager;
    }

    private ExecutorService threadPool;
    private Thread clienThread;

    public Serveur() {
        clientManager = new ClientManager();
        threadPool = Executors.newCachedThreadPool();
        SettingMapper mapper = new SettingMapper("src/Serveur/Setting.json");
        Map<String, Object> setting = mapper.getJsonObject();
        
        cacheDuration = (int) setting.get("cache duration");
        cacheLength = (int) setting.get("max cache size");

        localCache = new Cache(cacheLength, cacheDuration);
        enablePrompt();
    }

    public void start() {
        SettingMapper mapper = new SettingMapper("src/Serveur/Setting.json");
        Map<String, Object> setting = mapper.getJsonObject();

        serverPort = (int) setting.get("proxy port");
        nextServer = (String) setting.get("server Source");
        nextServerPort = (int) setting.get("server Port");



        boolean isClientEnable = Boolean.parseBoolean((String) setting.get("enable client"));
        boolean isGUIpanelEnable = Boolean.parseBoolean((String) setting.get("guiPanel enable"));

        active = true;
        running = true;

        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isClientEnable)
            enableClient();
        if (isGUIpanelEnable)
            enableGUIpanel();

        System.out.println("server started on port :" +serverPort);
        try(Socket test = new Socket(nextServer,nextServerPort)) {
            test.close();
        } catch (Exception e) {
            System.out.println("Warning : noting is running on server"+nextServer+":"+nextServerPort);
        }
    }

    public void stop() throws Exception {
        active = false;
        running = false;
        clienThread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("impossible de stopper le serveur socket : \n");
        }
    }

    public void enableGUIpanel() {
        cacheGUI = new CacheManager(this);
        localCache.setCacheGUI(cacheGUI);
    }

    public void enablePrompt() {
        promptActive=true;
        inputScanner = new Scanner(System.in);
        final CommandM commandManager = new CommandM(this,inputScanner);
        Thread promptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Scanner commandListener = new Scanner(System.in);
                System.out.println("---------------Welcome to the proxy cache controler------------------------");
                while (promptActive) {
                    System.out.print(">>>");
                    String commande = inputScanner.nextLine();
                    commandManager.runCommand(commande);
                }
            }
        });
        promptThread.start();
    }

    public void enableClient() {
        final Serveur currentServeur = this;
        clienThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (running) {
                        if (active && !serverSocket.isClosed()) {
                            Socket clientSocket = serverSocket.accept(); // Accepter une connexion client

                            Client c = null;
                            if (!clientManager.isInHistory((Inet4Address) clientSocket.getInetAddress())) {
                                c = new Client((Inet4Address) clientSocket.getInetAddress(), currentServeur);
                                clientManager.addInHistory(c);
                            } else {
                                c = clientManager.getCLient((Inet4Address) clientSocket.getInetAddress());
                            }

                            threadPool.submit(new ClientHandler(clientSocket, currentServeur, c));
                        }
                    }
                } catch (IOException e) {
                    if (running) {
                        e.printStackTrace();
                    } else {
                        System.out.println("Serveur arrêté, thread d'acceptation fermé.");
                        System.out.print(">>>");
                    }
                }
            }
        });
        clienThread.start();
    }

    public void writeFileContent(Socket clientSocket, Requette request, Client client) {

        try {
            OutputStream output = clientSocket.getOutputStream();

            byte[] bytes = null;

            if (!client.isInUserCache(request.getResource())) {
                String serverResponse = getNextServerResponse(request);
                bytes = serverResponse.getBytes("UTF-8");
                client.addUserCache(request.getResource(), bytes);
            } else {
                bytes = client.getInCache(request.getResource());
            }

            if (bytes != null) {
                output.write(bytes);
                output.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getNextServerResponse(Requette query) {
        StringBuilder response = new StringBuilder();

        try {
            Socket nextServerSocket = new Socket(nextServer, nextServerPort);
            if (!nextServerSocket.isClosed()) {
                OutputStream output = nextServerSocket.getOutputStream();
                output.write(query.getBasicRequest(nextServer).getBytes("UTF-8"));

                output.flush();

                BufferedReader in = new BufferedReader(new InputStreamReader(nextServerSocket.getInputStream()));
                boolean typeHeaders = true;

                String recivedLine = new String();

                String contentType = new String();
                while ((recivedLine = in.readLine()) != null) {
                    if (recivedLine.isEmpty()) {
                        typeHeaders = false;
                    }
                    if (typeHeaders) {
                        if (recivedLine.toLowerCase().startsWith("content-type")) {
                            contentType = recivedLine.split(":")[1].trim();
                            response.append(FileManager.getHeader(contentType));
                        }
                    } else {
                        response.append(recivedLine);
                    }
                }
                nextServerSocket.close();
            }
        } catch (UnknownHostException e) {
            System.out.println("host exception");
        } catch (IOException e) {
            return null;
        }
        return response.toString();
    }

    public long getTotalCacheSize() {
        return localCache.getCacheSize();
    }

    public Cache getServeurCache() {
        return localCache;
    }

    public void restartServer() {
        try {
            System.out.println("Arrêt du serveur...");
            running = false;
            if (clienThread != null && clienThread.isAlive()) {
                clienThread.interrupt();
                clienThread = null;
            }

            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
                System.out.println("Serveur arrêté.");
            }

            System.out.println("Cache réinitialisé.");

            running = true;

            serverSocket = new ServerSocket(serverPort);

            enableClient();

            System.out.println("Serveur prêt à accepter des connexions.");
        } catch (IOException e) {
            System.err.println("Erreur lors du redémarrage du serveur: " + e.getMessage());
        }
    }
}
