package fr.tp.inf112.projects.robotsim.service;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.robotsim.app.SimulatorController;
import fr.tp.inf112.projects.robotsim.model.Factory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.logging.Logger;

public class RemoteSimulatorController extends SimulatorController {

    private static final Logger logger = Logger.getLogger(RemoteSimulatorController.class.getName());
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String microserviceBaseUrl;

    public RemoteSimulatorController(String microserviceBaseUrl) {
        super(null, null); // No local factory or persistence manager required
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        this.microserviceBaseUrl = microserviceBaseUrl;
    }

    /**
     * Start the simulation for the specified factory.
     *
     * @param factoryId The ID of the factory to simulate.
     * @return true if the simulation was started successfully, false otherwise.
     */
    @Override
    public void startAnimation() {
        try {
            logger.info(getCanvas().getId());
            String url = "http://" + microserviceBaseUrl + ":2000/simulation/start" + "?factoryPath=" + getCanvas().getId();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

            logger.info("Sending POST request to " + url);
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Response: " + response.statusCode() + " - " + response.body());
            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                logger.info("Simulation started successfully for factory ID: " + getCanvas().getId());

                // Activer la simulation localement
                ((Factory) getCanvas()).startSimulation();
                // Appeler updateViewer pour synchroniser les données
                updateViewer();
            } else {
                logger.warning("Failed to start simulation for factory ID: " + getCanvas().getId());
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Error starting simulation: " + e.getMessage());
        }
    }



    /**
     * Stop the simulation for the specified factory.
     */
    @Override
    public void stopAnimation() {
        try {
            String url = "http://" + microserviceBaseUrl + ":2000/simulation/stop" + "?factoryPath=" + getCanvas().getId();

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE() // DELETE request
                .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Response: " + response.statusCode() + " - " + response.body());
            if (response.statusCode() == 200 && Boolean.parseBoolean(response.body())) {
                logger.info("Simulation stopped successfully for factory ID: " + getCanvas().getId());

                // Arrêter la simulation localement
                ((Factory) getCanvas()).stopSimulation();
            } else {
                logger.warning("Failed to stop simulation for factory ID: " + getCanvas().getId());
            }
        } catch (IOException | InterruptedException e) {
            logger.severe("Error stopping simulation: " + e.getMessage());
        }
    }


    /**
     * Retrieve the current state of the factory simulation.
     *
     * @return The updated factory object.
     */
    public Factory getFactory() {
        try {
            final URI uri = new URI("http", null, microserviceBaseUrl, 2000, "/simulation/retrieve" + getCanvas().getId(), null, null);
            HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
            logger.info("Canvas updated and observers notified.");
            logger.info("Fetching factory state from URL: " + uri);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                Factory factory = objectMapper.readValue(response.body(), Factory.class);
                logger.info("Factory retrieved successfully: ");
                return factory;
            } else {
                logger.warning("Failed to retrieve factory for ID: " + getCanvas().getId());
            }
        } catch (Exception e) {
            logger.severe("Error retrieving factory: " + e.getMessage());
        }
        return null;
    }

    /**
     * Periodically fetch updates from the remote simulation.
     */
    private void updateViewer() {
        new Thread(() -> {
            logger.info("Starting updateViewer thread...");
            while (((Factory) getCanvas()).isSimulationStarted()) {
                try {
                    Factory updatedFactory = getFactory();
                    if (updatedFactory != null) {
                        setCanvas(updatedFactory);
                    }
                    Thread.sleep(100); // Mise à jour toutes les 100 ms
                } catch (InterruptedException e) {
                    logger.warning("Update viewer thread interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Réinterrompre le thread
                    break;
                } catch (Exception e) {
                    logger.severe("Error updating viewer: " + e.getMessage());
                }
            }
            logger.info("Exiting updateViewer thread.");
        }).start();
    }




    /**
    * {@inheritDoc}
    */
    @Override
    public void setCanvas(final Canvas canvasModel) {
        if (canvasModel == null) {
            logger.warning("Provided canvasModel is null. Skipping setCanvas operation.");
            return;
        }

        if (!(canvasModel instanceof Factory)) {
            logger.warning("Provided canvasModel is not an instance of Factory. Skipping setCanvas operation.");
            return;
        }

        Factory factoryModel = (Factory) canvasModel;

        if (getCanvas() == null) {
            logger.warning("Current canvas is null. Initializing with a new Factory instance.");
            super.setCanvas(new Factory(200, 200, "Default Factory"));
            
        }

        Factory currentFactory = (Factory) getCanvas();

        final List<Observer> observers = currentFactory.getObservers();
        super.setCanvas(factoryModel);
        for (final Observer observer : observers) {
            factoryModel.addObserver(observer);
        }
        factoryModel.notifyObservers();
        logger.info("Canvas updated and observers notified.");
    }
}
