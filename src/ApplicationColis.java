import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApplicationColis extends JFrame {
    private final GestionColis gestion = new GestionColis();
    private final DefaultTableModel modeleTableau;
    private final JTable tableauColis;
    private final JTextField champNomProprietaire;
    private final JLabel etiquetteStatut;
    private final ExecutorService serviceExecutors = Executors.newCachedThreadPool();
    private final ScheduledExecutorService servicePlanifie = Executors.newScheduledThreadPool(1);
    private final List<String> nomsProprietaires = new ArrayList<>();
    private final DefaultListModel<String> modeleListe = new DefaultListModel<>();
    private final JList<String> listeProprietaires;

    public ApplicationColis() {
        setTitle("Système de Gestion des Colis");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Titre
        JLabel titre = new JLabel("Système de Gestion des Colis", SwingConstants.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        add(titre, gbc);

        // Section d'entrée
        JPanel panneauEntree = new JPanel(new FlowLayout());
        champNomProprietaire = new JTextField(20);
        JButton boutonAjouterProprietaire = new JButton("Ajouter Propriétaire");
        JButton boutonAjouterColis = new JButton("Ajouter Colis");

        panneauEntree.add(new JLabel("Nom du propriétaire:"));
        panneauEntree.add(champNomProprietaire);
        panneauEntree.add(boutonAjouterProprietaire);
        panneauEntree.add(boutonAjouterColis);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        add(panneauEntree, gbc);

        // Liste des propriétaires
        listeProprietaires = new JList<>(modeleListe);
        JScrollPane ascenseurListe = new JScrollPane(listeProprietaires);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        add(ascenseurListe, gbc);

        // Tableau
        String[] nomsColonnes = {"ID Colis", "Propriétaire", "Statut"};
        modeleTableau = new DefaultTableModel(nomsColonnes, 0);
        tableauColis = new JTable(modeleTableau);
        JScrollPane ascenseur = new JScrollPane(tableauColis);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 0.7;
        add(ascenseur, gbc);

        // Étiquette de statut
        etiquetteStatut = new JLabel("", SwingConstants.CENTER);
        etiquetteStatut.setForeground(Color.GREEN);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.weightx = 1;
        add(etiquetteStatut, gbc);

        // Action Ajouter Propriétaire
        boutonAjouterProprietaire.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nomProprietaire = champNomProprietaire.getText().trim();
                if (!nomProprietaire.isEmpty()) {
                    nomsProprietaires.add(nomProprietaire);
                    modeleListe.addElement(nomProprietaire);
                    champNomProprietaire.setText("");
                } else {
                    afficherAlerte("Veuillez entrer un nom de propriétaire.");
                }
            }
        });

        // Action Ajouter Colis
        // Action Ajouter Colis
boutonAjouterColis.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        String nomProprietaire = listeProprietaires.getSelectedValue();
        if (nomProprietaire != null) {
            serviceExecutors.submit(() -> {
                try {
                    Colis nouveauColis = new Colis(nomProprietaire);
                    gestion.enregistrerColis(nouveauColis);
                    
                    // Remove the owner from the list after adding a parcel
                    SwingUtilities.invokeLater(() -> {
                        int selectedIndex = listeProprietaires.getSelectedIndex();
                        if (selectedIndex != -1) {
                            nomsProprietaires.remove(selectedIndex);
                            modeleListe.remove(selectedIndex);
                        }
                        
                        mettreAJourTableau();
                        mettreAJourEtiquetteStatut("Colis #" + nouveauColis.getId() + " ajouté avec succès!");
                    });
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }
            });
        } else {
            afficherAlerte("Veuillez sélectionner un propriétaire.");
        }
    }
});

        // Démarrer la simulation de livraison
        demarrerSimulationLivraison();
    }

    private void demarrerSimulationLivraison() {
        ServiceLivraison serviceLivraison = new ServiceLivraison(gestion, this::mettreAJourTableau);
        serviceExecutors.submit(serviceLivraison);
    }

    private void mettreAJourTableau() {
        SwingUtilities.invokeLater(() -> {
            // Effacer les lignes existantes
            modeleTableau.setRowCount(0);
            
            // Ajouter les colis actuels
            for (Colis colis : gestion.getColisMap().values()) {
                modeleTableau.addRow(new Object[]{
                    colis.getId(), 
                    colis.getNomProprietaire(), 
                    colis.getStatut()
                });
            }
        });
    }

    private void mettreAJourEtiquetteStatut(String message) {
        SwingUtilities.invokeLater(() -> {
            etiquetteStatut.setText(message);
            
            // Effacer le message après 3 secondes
            servicePlanifie.schedule(() -> 
                SwingUtilities.invokeLater(() -> etiquetteStatut.setText("")),
                3, TimeUnit.SECONDS
            );
        });
    }

    private void afficherAlerte(String message) {
        JOptionPane.showMessageDialog(
            this, 
            message, 
            "Entrée invalide", 
            JOptionPane.WARNING_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationColis app = new ApplicationColis();
            app.setVisible(true);
        });
    }

    @Override
    public void dispose() {
        serviceExecutors.shutdownNow();
        servicePlanifie.shutdownNow();
        super.dispose();
    }
}