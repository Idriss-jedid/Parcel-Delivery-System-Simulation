import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class GestionColis {
    private final ConcurrentHashMap<Integer, Colis> colisMap = new ConcurrentHashMap<>();
    private final Semaphore semaphore = new Semaphore(1);

    public void enregistrerColis(Colis nouveauColis) throws InterruptedException {
        semaphore.acquire();
        try {
            colisMap.put(nouveauColis.getId(), nouveauColis);
            System.out.println("Colis " + nouveauColis.getId() + " enregistré.");
        } finally {
            semaphore.release();
        }
    }

    public ConcurrentHashMap<Integer, Colis> getColisMap() {
        return colisMap;
    }
}