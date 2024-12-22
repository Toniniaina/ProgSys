import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Démarrer le serveur web dans un thread séparé
        Thread serveurThread = new Thread(() -> {
            try {
                ServeurWeb.main(args);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Démarrer le navigateur dans un autre thread
        Thread navigateurThread = new Thread(() -> {
            Navigateur.main(args);
        });

        // Lancer les deux threads
        serveurThread.start();
        navigateurThread.start();
    }
}

