package src.Serveur;

import java.net.Inet4Address;
import java.util.ArrayList;

public class ClientManager {
    private ArrayList<Client> clientList;

    public ClientManager(){
        clientList = new ArrayList<Client>();
    }

    public ArrayList<Client> getClientList() {
        return clientList;
    }

    public void setClientList(ArrayList<Client> clientList) {
        this.clientList = clientList;
    }

    public void addInHistory(Client c){
        clientList.add(c);
    }

    public boolean isInHistory(Inet4Address clientAdress){
        for (Client client : clientList) {
            if(client.getClientAdress().equals(clientAdress)){
                return true;
            }
        }

        return false;
    }

    public Client getCLient(Inet4Address address){
        Client c = null;
        for (Client client : clientList) {
            if(client.getClientAdress().equals(address)){
                c = client;
            }
        }
        return c;
    }
}
