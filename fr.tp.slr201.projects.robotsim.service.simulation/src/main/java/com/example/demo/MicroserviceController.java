package com.example.demo;

import fr.tp.inf112.projects.robotsim.server.RemoteFactoryPersistenceManager;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

@RestController
@RequestMapping("/simulation")
public class MicroserviceController {

    private static final Logger logger = Logger.getLogger(MicroserviceController.class.getName());
    private final RemoteFactoryPersistenceManager persistenceManager;
    private final Map<String, Canvas> activeSimulations = new ConcurrentHashMap<>();
    private final CanvasChooser canvasChooser;

    public MicroserviceController() throws IOException {
        // Initialisation du CanvasChooser
        this.canvasChooser = new FileCanvasChooser("factory", "Puck Factory");

        // Connexion au serveur de persistance
        Socket socket = new Socket("localhost", 8080);
        logger.info("Connected to the persistence server on localhost:8080.");

        // Initialisation du RemoteFactoryPersistenceManager
        this.persistenceManager = new RemoteFactoryPersistenceManager(canvasChooser, socket);
        logger.info("RemoteFactoryPersistenceManager initialized successfully.");
    }

    /**
     * Démarrer une simulation identifiée par son ID.
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<String> startSimulation(@PathVariable String id) {
        try {
            Canvas canvas = persistenceManager.read(id);
            if (canvas == null) {
                logger.warning("Factory model not found for ID: " + id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Simulation not found.");
            }
            activeSimulations.put(id, canvas);
            logger.info("Simulation started successfully for ID: " + id);
            return ResponseEntity.ok("Simulation started.");
        } catch (IOException e) {
            logger.severe("Error starting simulation for ID: " + id + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    /**
     * Obtenir l'état actuel d'une simulation.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Canvas> getSimulation(@PathVariable String id) {
        Canvas canvas = activeSimulations.get(id);
        if (canvas != null) {
            logger.info("Returning simulation state for ID: " + id);
            return ResponseEntity.ok(canvas);
        } else {
            logger.warning("Simulation not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Arrêter une simulation.
     */
    @DeleteMapping("/stop/{id}")
    public ResponseEntity<String> stopSimulation(@PathVariable String id) {
        Canvas canvas = activeSimulations.remove(id);
        if (canvas == null) {
            logger.warning("Simulation not found for ID: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Simulation not found.");
        }
        try {
            persistenceManager.persist(canvas);
            logger.info("Simulation stopped and persisted successfully for ID: " + id);
            return ResponseEntity.ok("Simulation stopped.");
        } catch (IOException e) {
            logger.severe("Error persisting simulation for ID: " + id + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}
