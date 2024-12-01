package com.example.demo;

import fr.tp.inf112.projects.robotsim.app.SimulatorController;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
@RestController
@RequestMapping("/simulation")
public class MicroserviceController {

    private static final Logger logger = Logger.getLogger(MicroserviceController.class.getName());
    private final Map<String, Factory> simulatedFactories = new HashMap<>();
    private final FactoryPersistenceManager persistenceManager;

    public MicroserviceController() {
        FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
        this.persistenceManager = new FactoryPersistenceManager(canvasChooser);
    }

    /**
     * Normalise un chemin pour garantir la cohérence des clés.
     *
     * @param path Chemin à normaliser
     * @return Chemin absolu et canonique
     */
    private String normalizePath(String path) throws IOException {
        return new File(path).getCanonicalPath();
    }

    @PostMapping("/start")
    public boolean startSimulation(@RequestParam String factoryPath) {
        try {
            String normalizedPath = normalizePath(factoryPath);
            logger.info("Starting simulation for normalized path: " + normalizedPath);

            Factory factoryModel;

            // Vérifier si le fichier existe
            File factoryFile = new File(normalizedPath);
            if (factoryFile.exists() && factoryFile.isFile()) {
                logger.info("Factory file found at path: " + normalizedPath);
                factoryModel = (Factory) persistenceManager.read(normalizedPath);
            } else {
                logger.warning("Factory file not found at path: " + normalizedPath + ". Attempting SocketServer.");
                factoryModel = fetchFactoryFromSocketServer(normalizedPath);

                if (factoryModel == null) {
                    logger.severe("Factory model not found for path: " + normalizedPath);
                    return false;
                }
            }

            // Ajouter le modèle à la liste des simulations
            simulatedFactories.put(normalizedPath, factoryModel);

            // Démarrer la simulation
            SimulatorController simulatorController = new SimulatorController(factoryModel, persistenceManager);
            simulatorController.startAnimation();

            logger.info("Simulation started for path: " + normalizedPath);
            return true;
        } catch (Exception e) {
            logger.severe("Failed to start simulation for path: " + factoryPath + ": " + e.getMessage());
            return false;
        }
    }


    @GetMapping("/retrieve/{factoryPath:.+}")
    public Factory retrieveSimulation(@PathVariable String factoryPath) {
        try {
            String normalizedPath = normalizePath(factoryPath);
            logger.info("Retrieving simulation for normalized path: " + normalizedPath);

            Factory factoryModel = simulatedFactories.get(normalizedPath);
            if (factoryModel == null) {
                logger.warning("Factory model not found for path: " + normalizedPath);
                return null;
            }

            logger.info("Successfully retrieved factory model for path: " + normalizedPath);
            return factoryModel;
        } catch (IOException e) {
            logger.severe("Error normalizing path for retrieveSimulation: " + e.getMessage());
            return null;
        }
    }


    @DeleteMapping("/stop/{factoryPath:.+}")
    public boolean stopSimulation(@PathVariable String factoryPath) {
        try {
            String normalizedPath = normalizePath(factoryPath);
            logger.info("Stopping simulation for normalized path: " + normalizedPath);

            Factory factoryModel = simulatedFactories.get(normalizedPath);
            if (factoryModel == null) {
                logger.warning("Factory model not found for path: " + normalizedPath);
                return false;
            }

            // Arrêter la simulation
            SimulatorController simulatorController = new SimulatorController(factoryModel, persistenceManager);
            simulatorController.stopAnimation();

            // Supprimer de la liste des simulations
            simulatedFactories.remove(normalizedPath);
            logger.info("Simulation stopped and removed for path: " + normalizedPath);
            return true;
        } catch (IOException e) {
            logger.severe("Error normalizing path for stopSimulation: " + e.getMessage());
            return false;
        }
    }


    private Factory fetchFactoryFromSocketServer(String factoryId) {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(factoryId);
            out.flush();

            Object response = in.readObject();
            if (response instanceof Factory factoryModel) {
                logger.info("Factory model retrieved successfully for path: " + factoryId);
                return factoryModel;
            } else {
                logger.warning("Unexpected response from SocketServer for path: " + factoryId);
                return null;
            }
        } catch (Exception e) {
            logger.severe("Error communicating with SocketServer for factory path " + factoryId + ": " + e.getMessage());
            return null;
        }
    }
}
