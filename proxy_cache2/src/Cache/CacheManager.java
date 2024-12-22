package src.Cache;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import javax.swing.*;

import src.Serveur.Serveur;

public class CacheManager extends JFrame {

    private Serveur serveurProxy;

    private JPanel mainPanel;
    private CardLayout cardLayout;

    private JPanel statsPanel;
    private JPanel cacheConfigPanel;
    private JPanel cacheListPanel;

    private JTextArea statsArea;
    private JTextField ttlField;
    private JCheckBox enableCacheCheckBox;

    public CacheManager(Serveur proxyServer) {
        this.serveurProxy = proxyServer;
        if (serveurProxy == null) {
            System.out.println("Serveur proxy est null.");
        }
        initComponents();
    }

    private void initComponents() {

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException
                | IllegalAccessException e) {
            e.printStackTrace();
        }

        setTitle("Gestion du Proxy Cache");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JMenuBar menuBar = new JMenuBar();
        createMenus(menuBar);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, "StatsPanel");

        cacheConfigPanel = createCacheConfigPanel();
        mainPanel.add(cacheConfigPanel, "CacheConfigPanel");

        cacheListPanel = createCacheListPanel();
        JScrollPane listContainer = new JScrollPane(cacheListPanel);
        listContainer.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        listContainer.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        listContainer.setPreferredSize(new Dimension(mainPanel.getWidth(), mainPanel.getHeight()));
        mainPanel.add(listContainer, "ListPanel");

        setJMenuBar(menuBar);
        add(mainPanel);

        setVisible(true);
    }

    private void createMenus(JMenuBar menuBar) {
        JMenu cacheMenu = new JMenu("Cache");
        JMenuItem enableCacheItem = new JMenuItem("Activer/Désactiver le Cache");
        enableCacheItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "CacheConfigPanel");
            }
        });
        JMenuItem configCacheItem = new JMenuItem("Configurer le Cache");
        configCacheItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "CacheConfigPanel");
            }
        });

        JMenuItem reloadConfigItem = new JMenuItem("restart config");
        reloadConfigItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                serveurProxy.restartServer();
                cardLayout.show(mainPanel, "CacheConfigPanel");
            }
        });

        cacheMenu.add(enableCacheItem);
        cacheMenu.add(configCacheItem);
        cacheMenu.add(reloadConfigItem);

        JMenu statsMenu = new JMenu("Statistiques");
        JMenuItem showStatsItem = new JMenuItem("Afficher les Statistiques");
        showStatsItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "StatsPanel");
                updateStatsPanel();
            }
        });
        statsMenu.add(showStatsItem);

        JMenu listMenu = new JMenu("List");
        JMenuItem showCacheContent = new JMenuItem("Gérer le contenue du cache");
        showCacheContent.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "ListPanel");
                updateCacheListPanel();
            }
        });

        listMenu.add(showCacheContent);

        menuBar.add(cacheMenu);
        menuBar.add(statsMenu);
        menuBar.add(listMenu);
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(240, 240, 240));

        statsArea = new JTextArea();
        statsArea.setEditable(false);
        statsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        statsArea.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(statsArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        updateStatsPanel();

        return panel;
    }

    public void updateStatsPanel() {
        long cacheSize = serveurProxy.getTotalCacheSize();
        StringBuilder statsInfo = new StringBuilder();
        statsInfo.append("Statistique du cache\n");
        statsInfo.append("maximum cache size : " + serveurProxy.getServeurCache().getMaxCacheSize() + " Ko\n");
        statsInfo.append("taille occupée : " + cacheSize + " Ko\n");
        String stats = statsInfo.toString();
        statsArea.setText(stats);
    }

    private JPanel createCacheConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4, 2, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel ttlLabel = new JLabel("TTL du cache (sec) :");
        ttlField = new JTextField("3600");
        enableCacheCheckBox = new JCheckBox("Activer le cache");

        JButton saveButton = new JButton("Sauvegarder");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ttl = ttlField.getText();
                boolean isCacheEnabled = enableCacheCheckBox.isSelected();

                JOptionPane.showMessageDialog(panel,
                        "Configuration sauvegardée :\nTTL: " + ttl + "\nCache activé: " + isCacheEnabled);
                updateCacheConfig(ttl, isCacheEnabled);
            }
        });

        panel.add(ttlLabel);
        panel.add(ttlField);
        panel.add(new JLabel());
        panel.add(enableCacheCheckBox);
        panel.add(new JLabel());
        panel.add(saveButton);

        return panel;
    }

    public void updateCacheConfig(String ttl, boolean isCacheEnabled) {
        ttlField.setText(ttl);
        enableCacheCheckBox.setSelected(isCacheEnabled);
    }

    public JPanel createCacheListPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        return panel;
    }

    private void updateCacheListPanel() {
        cacheListPanel.removeAll();
        ArrayList<String> cacheContent = serveurProxy.getServeurCache().getCaheContent();
        cacheListPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int i=0;
        for (String s : cacheContent) {
            gbc.gridy = i;
            gbc.gridx =0;

            JLabel label = new JLabel(s);
            label.setPreferredSize(new Dimension(mainPanel.getWidth()/2, 30));

            cacheListPanel.add(label,gbc);

            gbc.gridx=1;
            
            JPanel cacheInfo = new JPanel();
            cacheInfo.setLayout(new GridLayout(1, 2));
            long cacheSize = serveurProxy.getServeurCache().getCacheSize(s);
            String info = String.valueOf(cacheSize).concat(" Ko");
            cacheInfo.add(new JLabel(info));
            cacheInfo.add(new JButton("rm"));

            cacheInfo.setPreferredSize(new Dimension(mainPanel.getWidth()/2-100, 30));
            cacheListPanel.add(cacheInfo,gbc);
            i++;
        }

        this.repaint();
        this.revalidate();
    }

    public void reloadPanel() {
        updateStatsPanel();
        updateCacheListPanel();
    }
}
