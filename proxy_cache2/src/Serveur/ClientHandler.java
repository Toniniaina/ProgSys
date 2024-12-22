package src.Serveur;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.Socket;

import src.Entities.Requette;

public class ClientHandler extends Thread {
    private Socket clientSocket;   
    private Serveur serveur;
    private Client client;


    public ClientHandler(Socket clientSocket, Serveur serveur, Client client) {
        this.clientSocket = clientSocket;
        this.serveur = serveur;
        this.client = client;
    }
    
    public void run() {
        try {
            InputStream input = clientSocket.getInputStream();
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(input));
            String line = clientReader.readLine();
            Requette query = new Requette(line);

            while ((line = clientReader.readLine()) != null && !line.isEmpty()) {
                query.addHeader(line);
            }

            serveur.writeFileContent(clientSocket, query, client);

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
