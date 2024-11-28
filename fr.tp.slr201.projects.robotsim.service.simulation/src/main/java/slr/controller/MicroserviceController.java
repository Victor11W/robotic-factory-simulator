package slr.controller;

import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/simulation")
public class MicroserviceController {

    private static final Logger logger = Logger.getLogger(MicroserviceController.class.getName());
    private final RemoteFactoryPersistenceManager persistenceManager;
    private final Map<String, Canvas> activeSimulations = new HashMap<>();

    public MicroserviceController() throws IOException {
        // Configuration du RemoteFactoryPersistenceManager pour communiquer avec FactoryServer
        Socket socket = new Socket("localhost", 8080); // Connexion au serveur distant (FactoryServer)
        CanvasChooser canvasChooser = new MyCanvasChooser(); // Adapter selon ton implémentation
        this.persistenceManager = new RemoteFactoryPersistenceManager(canvasChooser, socket);
    }

    /**
     * Démarrer une simulation identifiée par son ID.
     *
     * @param id Identifiant de la simulation
     * @return Boolean indiquant si la simulation a démarré avec succès
     */
    @PostMapping("/startSimulation/{id}")
    public ResponseEntity<Boolean> startSimulation(@PathVariable String id) {
        try {
            logger.info("Attempting to start simulation for ID: " + id);

            // Lire le modèle de Factory depuis le serveur
            Canvas canvas = persistenceManager.read(id);
            if (canvas == null) {
                logger.warning("Factory model not found for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
            }

            // Ajouter à la liste des simulations actives
            activeSimulations.put(id, canvas);

            // Démarrer la simulation (implémentation spécifique)
            canvas.startSimulation();
            logger.info("Simulation started successfully for ID: " + id);

            return ResponseEntity.ok(true);
        } catch (IOException e) {
            logger.severe("Error while starting simulation for ID: " + id + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    /**
     * Obtenir l'état actuel d'une simulation identifiée par son ID.
     *
     * @param id Identifiant de la simulation
     * @return Canvas représentant l'état actuel de la simulation
     */
    @GetMapping("/getSimulation/{id}")
    public ResponseEntity<Canvas> getSimulation(@PathVariable String id) {
        logger.info("Fetching simulation state for ID: " + id);

        Canvas canvas = activeSimulations.get(id);
        if (canvas != null) {
            return ResponseEntity.ok(canvas);
        } else {
            logger.warning("Simulation not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Arrêter une simulation identifiée par son ID.
     *
     * @param id Identifiant de la simulation
     * @return Message confirmant l'arrêt de la simulation
     */
    @PostMapping("/stopSimulation/{id}")
    public ResponseEntity<String> stopSimulation(@PathVariable String id) {
        logger.info("Attempting to stop simulation for ID: " + id);

        Canvas canvas = activeSimulations.remove(id);
        if (canvas != null) {
            try {
                // Arrêter la simulation (implémentation spécifique)
                canvas.stopSimulation();

                // Persister l'état final du Canvas
                persistenceManager.persist(canvas);
                logger.info("Simulation stopped and persisted for ID: " + id);

                return ResponseEntity.ok("Simulation stopped successfully.");
            } catch (IOException e) {
                logger.severe("Error while persisting canvas for ID: " + id + " - " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error persisting the canvas.");
            }
        } else {
            logger.warning("Simulation not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Simulation not found.");
        }
    }
}
