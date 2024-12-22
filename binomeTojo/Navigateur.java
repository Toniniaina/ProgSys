import javafx.application.Application;
        import javafx.beans.value.ChangeListener;
        import javafx.scene.Scene;
        import javafx.scene.control.Button;
        import javafx.scene.control.Tab;
        import javafx.scene.control.TabPane;
        import javafx.scene.control.TextField;
        import javafx.scene.layout.BorderPane;
        import javafx.scene.layout.HBox;
        import javafx.scene.layout.Priority;
        import javafx.scene.layout.VBox;
        import javafx.scene.web.WebEngine;
        import javafx.scene.web.WebView;
        import javafx.stage.Stage;
        import org.w3c.dom.Document;
        import org.w3c.dom.NodeList;

        import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;
        import java.io.File;
        import java.util.ArrayList;
        import java.util.List;

public class Navigateur extends Application {
    private static String BASE_URL = "http://localhost:1566";  // Valeur par défaut
    private VBox historyPanel;  // Panneau pour l'historique
    private VBox cachePanel;  // Panneau pour le cache
    private Button closeHistoryButton;  // Bouton pour fermer l'historique
    private Button closeCacheButton;  // Bouton pour fermer le cache
    private List<String> globalHistory = new ArrayList<>(); // Historique global pour tous les onglets

    public static void main(String[] args) {
        // Charger la configuration depuis le fichier XML
        loadConfig();
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Navigateur Web");
        BorderPane root = new BorderPane();

        // Création du TabPane pour gérer les onglets
        TabPane tabPane = new TabPane();
        tabPane.setId("tabPane"); // Ajout de l'ID pour identifier facilement le TabPane
        root.setCenter(tabPane);

        // Ajouter un onglet initial
        Tab initialTab = createTab(BASE_URL);
        tabPane.getTabs().add(initialTab);

        // Ajouter l'onglet "+" pour ajouter de nouveaux onglets
        Tab addTab = createAddTab(tabPane);
        tabPane.getTabs().add(addTab);

        // Créer et initialiser le panneau d'historique
        historyPanel = new VBox();
        historyPanel.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 10;");
        historyPanel.setVisible(false); // Initialement masqué
        historyPanel.setManaged(false); // Ne doit pas occuper d'espace tant qu'il n'est pas visible

        // Créer et initialiser le panneau de cache
        cachePanel = new VBox();
        cachePanel.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 10;");
        cachePanel.setVisible(false); // Initialement masqué
        cachePanel.setManaged(false); // Ne doit pas occuper d'espace tant qu'il n'est pas visible

        // Placer les panneaux à droite
        root.setRight(historyPanel);  // Placer le panneau d'historique
        root.setLeft(cachePanel);  // Placer le panneau de cache

        // Boutons de gestion d'historique et de cache
        closeHistoryButton = new Button("❌ Fermer");
        closeHistoryButton.setOnAction(event -> hideHistoryPanel());  // Masquer l'historique

        closeCacheButton = new Button("❌ Fermer");
        closeCacheButton.setOnAction(event -> hideCachePanel());  // Masquer le cache

        // Gérer le déplacement vers le dernier onglet ("+") pour l'ajout
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, selectedTab) -> {
            if (selectedTab == addTab) {
                Tab dynamicTab = createTab(BASE_URL);
                tabPane.getTabs().add(tabPane.getTabs().size() - 1, dynamicTab); // Ajouter avant l'onglet "+"
                tabPane.getSelectionModel().select(dynamicTab); // Sélectionner le nouvel onglet
            }
        });

        // Création de la scène
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    // Méthode pour créer un onglet avec des fonctionnalités de navigation
    private Tab createTab(String url) {
        // Création du champ de recherche et des boutons de navigation
        TextField urlField = new TextField(url);
        Button goButton = new Button("➡"); // Symbole pour "aller"
        Button refreshButton = new Button("🔄"); // Symbole pour "recharger"
        Button backButton = new Button("⬅"); // Symbole pour "retour"
        Button forwardButton = new Button("➡️"); // Symbole pour "avancer"
        Button darkModeButton = new Button("🌙"); // Symbole pour "mode sombre"
        Button historyButton = new Button("🕒"); // Symbole pour "historique"
        Button cacheButton = new Button("🧳 Cache"); // Symbole pour afficher/vider le cache

        // WebView et WebEngine
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.load(url);

        // Synchronisation de l'URL dans le champ de texte
        ChangeListener<String> urlListener = (observable, oldValue, newValue) -> urlField.setText(newValue);
        webEngine.locationProperty().addListener(urlListener);

        // Mise à jour automatique du titre de l'onglet
        Tab tab = new Tab("Nouvel Onglet");
        webEngine.titleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                tab.setText(newValue);
            } else {
                tab.setText("Nouvel Onglet");
            }
        });

        // Historique pour gérer "Retour" et "Avancer"
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (!globalHistory.contains(newValue)) {
                globalHistory.add(newValue);  // Ajouter l'URL à l'historique global
            }
        });

        // Mode sombre pour la page actuelle (persistant)
        final boolean[] isDarkMode = {false};
        darkModeButton.setOnAction(event -> {
            isDarkMode[0] = !isDarkMode[0];
            applyDarkMode(webEngine, isDarkMode[0]);
            darkModeButton.setText(isDarkMode[0] ? "☀" : "🌙");
        });

        // Appliquer le mode sombre à chaque chargement de page
        webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
            if (isDarkMode[0]) {
                applyDarkMode(webEngine, true);
            }
        });

        // Actions des boutons
        goButton.setOnAction(event -> {
            String urlInput = urlField.getText().trim();
            if (!urlInput.startsWith(BASE_URL)) {
                urlInput = BASE_URL + (urlInput.startsWith("/") ? "" : "/") + urlInput;
            }
            System.out.println("Chargement de l'URL : " + urlInput);
            webEngine.load(urlInput);
        });

        refreshButton.setOnAction(event -> webEngine.reload());

        backButton.setOnAction(event -> {
            int currentIndex = globalHistory.indexOf(webEngine.getLocation());
            if (currentIndex > 0) {
                webEngine.load(globalHistory.get(currentIndex - 1));
            }
        });

        forwardButton.setOnAction(event -> {
            int currentIndex = globalHistory.indexOf(webEngine.getLocation());
            if (currentIndex < globalHistory.size() - 1) {
                webEngine.load(globalHistory.get(currentIndex + 1));
            }
        });

        // Afficher/vider le cache
        cacheButton.setOnAction(event -> {
            List<String> cacheList = ServeurWeb.getCacheList();  // Obtenir la liste du cache
            System.out.println("Cache actuel : " + cacheList);  // Afficher le cache dans la console
            showCachePanel();  // Afficher le panneau du cache
        });

        // Afficher l'historique dans un panneau latéral
        historyButton.setOnAction(event -> {
            if (historyPanel.isVisible()) {
                hideHistoryPanel(); // Si déjà visible, masquer
            } else {
                showHistoryPanel(); // Afficher l'historique global
            }
        });

        // Barre de navigation
        HBox tabBar = new HBox(10);
        tabBar.setStyle("-fx-padding: 10;");
        HBox.setHgrow(urlField, Priority.ALWAYS);
        tabBar.getChildren().addAll(backButton, forwardButton, refreshButton, urlField, goButton, darkModeButton, historyButton, cacheButton);

        // Contenu de l'onglet
        BorderPane tabContent = new BorderPane();
        tabContent.setTop(tabBar);
        tabContent.setCenter(webView);

        tab.setClosable(true);
        tab.setContent(tabContent);

        return tab;
    }

    // Méthode pour appliquer le mode sombre
    private void applyDarkMode(WebEngine webEngine, boolean isDarkMode) {
        if (isDarkMode) {
            webEngine.executeScript("document.body.style.backgroundColor = '#121212';");
            webEngine.executeScript("document.body.style.color = 'white';");
        } else {
            webEngine.executeScript("document.body.style.backgroundColor = 'white';");
            webEngine.executeScript("document.body.style.color = 'black';");
        }
    }

    // Méthode pour créer un onglet spécial "+" pour ajouter des onglets
    private Tab createAddTab(TabPane tabPane) {
        Tab addTab = new Tab("➕");
        addTab.setClosable(false); // Cet onglet ne peut pas être fermé
        return addTab;
    }

    // Afficher le panneau d'historique
    private void showHistoryPanel() {
        historyPanel.getChildren().clear();  // Clear current history list
        historyPanel.getChildren().add(closeHistoryButton);  // Ajouter le bouton "Fermer"

        // Ajouter chaque entrée de l'historique global sous forme de bouton cliquable
        for (String url : globalHistory) {
            Button historyButton = new Button(url);
            historyButton.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 5;");
            historyButton.setOnAction(event -> openHistoryUrl(url));  // Ouvrir l'URL de l'historique
            historyPanel.getChildren().add(historyButton);
        }

        historyPanel.setVisible(true);
        historyPanel.setManaged(true);
    }

    // Cacher le panneau d'historique
    private void hideHistoryPanel() {
        historyPanel.setVisible(false);
        historyPanel.setManaged(false);
    }

    // Ouvrir une URL de l'historique
    private void openHistoryUrl(String url) {
        System.out.println("Ouvrir l'URL de l'historique : " + url);
    }

    // Afficher le panneau du cache
    // Méthode pour afficher le panneau du cache avec les options de suppression
    // Afficher le panneau du cache
    private void showCachePanel() {
        cachePanel.getChildren().clear();  // Clear current cache list
        cachePanel.getChildren().add(closeCacheButton);  // Ajouter le bouton "Fermer"

        // Ajouter chaque entrée du cache sous forme de bouton cliquable
        List<String> cacheList = ServeurWeb.getCacheList();  // Obtenir la liste du cache
        for (String cacheEntry : cacheList) {
            // Créer un bouton pour afficher la ressource du cache
            Button cacheButton = new Button(cacheEntry);
            cacheButton.setStyle("-fx-background-color: #f1f1f1; -fx-padding: 5;");

            // Créer un bouton de suppression pour cette ressource
            Button removeButton = new Button("Supprimer");
            removeButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-padding: 5;");

            // Ajouter l'action pour supprimer la ressource du cache
            removeButton.setOnAction(event -> {
                ServeurWeb.removeFromCache(cacheEntry);  // Supprimer la ressource du cache
                showCachePanel();  // Réactualiser le panneau du cache pour refléter les changements
            });

            // Ajouter les deux boutons au panneau de cache
            cachePanel.getChildren().addAll(cacheButton, removeButton);
        }

        // Ajouter un bouton pour vider tout le cache
        Button clearCacheButton = new Button("Vider le Cache");
        clearCacheButton.setOnAction(event -> {
            ServeurWeb.clearCache();  // Appeler la méthode pour vider le cache
            showCachePanel();  // Réafficher le cache mis à jour
        });

        // Ajouter le bouton "Vider le Cache"
        cachePanel.getChildren().add(clearCacheButton);

        // Rendre le panneau visible
        cachePanel.setVisible(true);
        cachePanel.setManaged(true);
    }


    // Méthode pour supprimer un élément spécifique du cache
    private void removeCacheItem(String resourcePath) {
        ServeurWeb.removeFromCache(resourcePath);  // Supprimer la ressource du cache du serveur
        showCachePanel();  // Réafficher le cache mis à jour
    }

    // Cacher le panneau du cache
    private void hideCachePanel() {
        cachePanel.setVisible(false);
        cachePanel.setManaged(false);
    }

    // Charger la configuration depuis le fichier XML
    private static void loadConfig() {
        try {
            File file = new File("config.xml");
            if (file.exists()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document document = builder.parse(file);
                document.getDocumentElement().normalize();
                NodeList urlList = document.getElementsByTagName("base-url");
                if (urlList.getLength() > 0) {
                    BASE_URL = urlList.item(0).getTextContent();
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors du chargement de la configuration : " + e.getMessage());
        }
    }
}
