package com.example.demo;

import fr.tp.inf112.projects.robotsim.app.SimulatorController;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;

import org.springframework.web.bind.annotation.*;

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

    // Map to store factories currently being simulated
    private final Map<String, Factory> simulatedFactories = new HashMap<>();
    private final FactoryPersistenceManager persistenceManager;

    public MicroserviceController() {
        FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
        this.persistenceManager = new FactoryPersistenceManager(canvasChooser);
    }

    /**
     * Start simulating a factory model identified by its ID.
     *
     * @param factoryId the ID of the factory to simulate
     * @return true if the simulation started successfully, false otherwise
     */
    @PostMapping("/start/{factoryId}")
    public boolean startSimulation(@PathVariable String factoryId) {
        try {
            logger.info("Starting simulation for factory ID: " + factoryId);

            Factory factoryModel;

            // Priorité : récupérer depuis le SocketServer
            factoryModel = fetchFactoryFromSocketServer(factoryId);

            if (factoryModel == null) {
                logger.warning("Factory ID " + factoryId + " not found on SocketServer. Attempting to load from persistence.");
                
                // Si non trouvé, essayer avec le persistenceManager
                factoryModel = (Factory) persistenceManager.read(factoryId);

                if (factoryModel == null) {
                    logger.severe("Factory model with ID " + factoryId + " not found.");
                    return false;
                }
            }

            // Ajouter le modèle à la liste des simulations
            simulatedFactories.put(factoryId, factoryModel);

            // Démarrer la simulation
            SimulatorController simulatorController = new SimulatorController(factoryModel, persistenceManager);
            simulatorController.startAnimation();

            logger.info("Simulation started for factory ID: " + factoryId);
            return true;
        } catch (Exception e) {
            logger.severe("Failed to start simulation for factory ID: " + factoryId + ": " + e.getMessage());
            return false;
        }
    }


    /**
     * Retrieve the current state of a factory model identified by its ID.
     *
     * @param factoryId the ID of the factory
     * @return the factory model currently being simulated, or null if not found
     */
    @GetMapping("/retrieve/{factoryId}")
    public Factory retrieveSimulation(@PathVariable String factoryId) {
        logger.info("Retrieving simulation for factory ID: " + factoryId);
        Factory factoryModel = simulatedFactories.get(factoryId);

        if (factoryModel == null) {
            logger.warning("Factory model with ID " + factoryId + " not found in active simulations.");
            return null;
        }

        logger.info("Successfully retrieved factory model for factory ID: " + factoryId);
        return factoryModel;
    }

    /**
     * Stop the simulation of a factory model identified by its ID.
     *
     * @param factoryId the ID of the factory to stop
     * @return true if the simulation was stopped successfully, false otherwise
     */
    @DeleteMapping("/stop/{factoryId}")
    public boolean stopSimulation(@PathVariable String factoryId) {
        logger.info("Stopping simulation for factory ID: " + factoryId);

        Factory factoryModel = simulatedFactories.get(factoryId);
        if (factoryModel == null) {
            logger.warning("Factory model with ID " + factoryId + " not found.");
            return false;
        }

        // Stop simulation
        SimulatorController simulatorController = new SimulatorController(factoryModel, persistenceManager);
        simulatorController.stopAnimation();

        // Remove from the simulated factories map
        simulatedFactories.remove(factoryId);
        logger.info("Simulation stopped and removed for factory ID: " + factoryId);
        return true;
    }
    
    /**
     * Fetch a factory model from the SocketServer using the factory ID.
     *
     * @param factoryId the ID of the factory to fetch
     * @return the retrieved factory model, or null if not found
     */
    private Factory fetchFactoryFromSocketServer(String factoryId) {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Envoyer l'ID de l'usine au SocketServer
            out.writeObject(factoryId);
            out.flush();

            // Recevoir le modèle de l'usine depuis le SocketServer
            Object response = in.readObject();
            if (response instanceof Factory factoryModel) {
                logger.info("Factory model retrieved successfully for ID: " + factoryId);
                return factoryModel;
            } else {
                logger.warning("Unexpected response from SocketServer for ID: " + factoryId);
                return null;
            }

        } catch (Exception e) {
            logger.severe("Error communicating with SocketServer for factory ID " + factoryId + ": " + e.getMessage());
            return null;
        }
    }

}
