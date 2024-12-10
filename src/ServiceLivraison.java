import java.util.concurrent.Semaphore;

public class ServiceLivraison implements Runnable {
    private final GestionColis gestion;
    private final Runnable onStatusChangeCallback;
    private final Semaphore semaphore;

    public ServiceLivraison(GestionColis gestion, Runnable onStatusChangeCallback) {
        this.gestion = gestion;
        this.onStatusChangeCallback = onStatusChangeCallback;
        this.semaphore = new Semaphore(1);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            for (Colis colis : gestion.getColisMap().values()) {
                if ("En attente".equals(colis.getStatut())) {
                    try {
                        semaphore.acquire();
                        colis.setStatut("En transit");
                        onStatusChangeCallback.run();
                        dormir();

                        colis.setStatut("Livré");
                        onStatusChangeCallback.run();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } finally {
                        semaphore.release();
                    }
                }
            }
        }
    }

    private void dormir() {
        try {
           int randomSleepTime = 5000 + (int)(Math.random() * 5000); // Random entre 5000 et 10000 ms
            Thread.sleep(randomSleepTime); // Temps d'attente aléatoire
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}