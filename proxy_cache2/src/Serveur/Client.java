package src.Serveur;

import java.net.Inet4Address;
import java.util.ArrayList;

public class Client {
    private Inet4Address inetAdress;
    private ArrayList<String> userCache;
    private Serveur proxyServeur;

    public Client(Inet4Address inetAdress, Serveur proxyServeur) {
        this.inetAdress = inetAdress;
        this.proxyServeur = proxyServeur;
        userCache = new ArrayList<String>();
    }

    public void addUserCache(String tmpName, byte[] content) {
        if (!proxyServeur.getServeurCache().isIncache(tmpName)) {
            proxyServeur.getServeurCache().addToCache(tmpName, content);
        }
        if (!userCache.contains(tmpName)) {
            userCache.add(tmpName);
        }
    }

    public void clearUserCache(){
        ArrayList<String> toRemove = new ArrayList<String>();
        for (String string : userCache) {
            proxyServeur.getServeurCache().removeFromCache(string);
            toRemove.add(string);
        }
        userCache.removeAll(toRemove);

    }

    public byte[] getInCache(String tmpName)  {
        byte[] content = null;
        if (userCache.contains(tmpName)) {
            content = proxyServeur.getServeurCache().getInCache(tmpName);
        }
        return content;
    }

    public Inet4Address getClientAdress(){
        return this.inetAdress;
    }

    public boolean isInUserCache(String tmpName){
        return userCache.contains(tmpName);
    }

}