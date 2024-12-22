package src.Serveur;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@FunctionalInterface
interface CommandWithArgs {
    void execute(String[] args);
}

public class CommandM {
    Serveur proxyServeur;
    Scanner inputScanner;
    Map<String, Runnable> listOfCommand;
    Map<String, CommandWithArgs> commands = new HashMap<>();


    public CommandM(Serveur proxyServeur,Scanner inputScanner) {
        this.proxyServeur = proxyServeur;
        this.inputScanner = inputScanner;
        setCommandList();
    }

    public void setCommandList() {
        listOfCommand = new HashMap<>();
        listOfCommand.put("info", () -> {
            System.out.print("max serveur size :" + proxyServeur.getServeurCache().getMaxCacheSize());
            System.out.println("Ko ||| current cache size :" + proxyServeur.getTotalCacheSize()+" Ko");
        });

        listOfCommand.put("stop",()-> {
            try {
                proxyServeur.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        listOfCommand.put("start", ()->{
            proxyServeur.start();
        });

        listOfCommand.put("exit", ()->{
            System.out.println("bye");
            System.exit(0);
        });

        listOfCommand.put("client_list", ()->{
            for(Client s : proxyServeur.getClientManager().getClientList()){
                System.out.println(s.getClientAdress());
            }
        });

        listOfCommand.put("clear", ()->{
            System.out.print("UserAdress :");
            String address = inputScanner.nextLine();
            try {
                Inet4Address clInet4Address = (Inet4Address) Inet4Address.getByName(address);
                Client c = proxyServeur.getClientManager().getCLient(clInet4Address);
                if(c!=null){
                    c.clearUserCache();
                }else{
                    System.out.println("user not found");
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });

        // commands.put("clear", (arguments)->{
        //     if(arguments.length < 1){
        //         System.out.println("usage clear <userAdress>");
        //     }else{
        //         try {
        //             Inet4Address clientAdress =(Inet4Address) Inet4Address.getByName(arguments[0]);
        //             System.out.println(clientAdress);
        //         } catch (UnknownHostException e) {
        //             e.printStackTrace();
        //         }
        //     }
        // });
    }

    public void runCommand(String commande) {
        if (listOfCommand.containsKey(commande)) {
            listOfCommand.get(commande).run();
        }else{
            // String[] input = commande.split(" ");
            // if(input.length > 1){
            //     String command = input[0];
            //     executeCommand(commands, command, null);

            // }
            System.out.println("command not found");
        }
    }

    // private static void executeCommand(Map<String,CommandWithArgs> commands,String command,String[] args){
    //     CommandWithArgs action = commands.get(command);
    //     if(action!=null){
    //         action.execute(args);
    //     } else{
    //         System.out.println("commande not found");
    //     }
    // }

}
