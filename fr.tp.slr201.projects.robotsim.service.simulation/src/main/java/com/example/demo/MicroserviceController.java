package com.example.demo;

import fr.tp.inf112.projects.robotsim.server.RemoteFactoryPersistenceManager;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/simulation")
public class MicroserviceController {

    private final RemoteFactoryPersistenceManager persistenceManager;
    private final Map<String, Canvas> activeSimulations = new ConcurrentHashMap<>();
    private final CanvasChooser canvasChooser;

    public MicroserviceController() throws IOException {
        // Initialisation des connexions au serveur de persistance
        this.canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
        this.persistenceManager = new RemoteFactoryPersistenceManager(canvasChooser, 8080);
    }

    /**
     * Démarrer une simulation identifiée par son ID.
     */
    @PostMapping("/start/{id}")
    public ResponseEntity<String> startSimulation(@PathVariable String id) {
        try {
            Canvas canvas = persistenceManager.read(id); // Récupère le modèle à partir du serveur
            if (canvas == null) {
                return createResponse("Simulation not found", HttpStatus.NOT_FOUND);
            }
            activeSimulations.put(id, canvas); // Ajoute le modèle aux simulations actives
            return createResponse("Simulation started", HttpStatus.OK);
        } catch (IOException e) {
            return createResponse("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtenir l'état actuel d'une simulation.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Canvas> getSimulation(@PathVariable String id) {
        Canvas canvas = activeSimulations.get(id);
        return (canvas != null) 
            ? ResponseEntity.ok(canvas) 
            : ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    /**
     * Arrêter une simulation.
     */
    @DeleteMapping("/stop/{id}")
    public ResponseEntity<String> stopSimulation(@PathVariable String id) {
        Canvas canvas = activeSimulations.remove(id); // Supprime la simulation des actives
        if (canvas == null) {
            return createResponse("Simulation not found", HttpStatus.NOT_FOUND);
        }
        try {
            persistenceManager.persist(canvas); // Persiste l'état final du modèle
            return createResponse("Simulation stopped", HttpStatus.OK);
        } catch (IOException e) {
            return createResponse("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Méthode utilitaire pour créer des réponses uniformes
    private ResponseEntity<String> createResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(message);
    }
}
