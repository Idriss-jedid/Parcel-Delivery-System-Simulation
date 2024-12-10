public class Colis {
    private static int idCompteur = 1; // Auto-incrémenté ID
    private final int id;
    private String statut; // "En attente", "En transit", "Livré"
    private final String nomProprietaire;

    public Colis(String nomProprietaire) {
        this.id = idCompteur++;
        this.nomProprietaire = nomProprietaire;
        this.statut = "En attente";
    }

    public int getId() {
        return id;
    }

    public synchronized String getStatut() {
        return statut;
    }

    public synchronized void setStatut(String statut) {
        this.statut = statut;
    }

    public String getNomProprietaire() {
        return nomProprietaire;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Propriétaire: " + nomProprietaire + ", Statut: " + statut;
    }
}