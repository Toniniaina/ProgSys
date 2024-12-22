import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            ConfigLoader config = ConfigLoader.getInstance("config.xml");
            String serverAddress = config.getConfigValue("serverBaseURL").replace("http://", "");
            int serverPort = Integer.parseInt(config.getConfigValue("port"));

            try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
                String command;
                while ((command = userInput.readLine()) != null) {
                    try (Socket socket = new Socket(serverAddress, serverPort);
                         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                        String requestLine = "GET /" + command + " HTTP/1.1";
                        out.println(requestLine);

                        String response;
                        while ((response = in.readLine()) != null) {
                            System.out.println(response);
                            if (response.equals("Cache Miss") || response.equals("Cache Hit")) {
                                break;
                            }
                        }

                    } catch (IOException e) {
                        System.err.println("Erreur de connexion au serveur : " + e.getMessage());
                    }

                    System.out.println("Entrez une autre commande ou tapez 'exit' pour quitter:");
                    if (command.equals("exit")) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur du client : " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Erreur de configuration : " + e.getMessage());
        }
    }
}
