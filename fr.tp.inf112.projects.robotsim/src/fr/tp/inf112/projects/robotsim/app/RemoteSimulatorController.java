package fr.tp.inf112.projects.robotsim.app;

import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.controller.CanvasViewerController;
import fr.tp.inf112.projects.canvas.controller.Observer;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasPersistenceManager;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.robotsim.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tp.inf112.projects.robotsim.model.path.CustomDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;

public class RemoteSimulatorController implements CanvasViewerController {
    private Factory factoryModel;
    private final CanvasPersistenceManager persistenceManager;
    //private final CanvasPersistenceManager persistenceManager;
    private final HttpClient httpClient;
    private final HttpRequest startRequest;
    private final HttpRequest stopRequest;
    private final HttpRequest getRequest;
    private final ObjectMapper objectMapper;

    private Thread animationThread;
    public RemoteSimulatorController(final CanvasPersistenceManager persistenceManager, final String factoryPath, final String factoryId) throws URISyntaxException {
        this.persistenceManager = persistenceManager;
        this.httpClient = HttpClient.newHttpClient();

        final URI startUri = new URI("http", null, "localhost", 8090, "/simulate", "id="+factoryPath, null);
        this.startRequest = HttpRequest.newBuilder().uri(startUri).POST(HttpRequest.BodyPublishers.noBody()).build();

        final URI stopUri = new URI("http", null, "localhost", 8090, "/stop", "id="+factoryId, null);
        this.stopRequest = HttpRequest.newBuilder().uri(stopUri).POST(HttpRequest.BodyPublishers.noBody()).build();

        final URI getUri = new URI("http", null, "localhost", 8090, "/get", "id="+factoryId, null);
        this.getRequest = HttpRequest.newBuilder().uri(getUri).GET().build();

        objectMapper = new ObjectMapper();
        PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(PositionedShape.class.getPackageName())
                /*.allowIfSubType(Area.class.getPackageName())
                .allowIfSubType(Room.class.getPackageName())
                .allowIfSubType(Factory.class.getPackageName())*/
                .allowIfSubType(Component.class.getPackageName())
                .allowIfSubType(BasicVertex.class.getPackageName())
                .allowIfSubType(ArrayList.class.getName())
                .allowIfSubType(LinkedHashSet.class.getName())
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        final Factory factory = new Factory(200, 200, "Simple Test Puck Factory");
        final Room room1 = new Room(factory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
        new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
        final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
        final Machine machine1 = new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");

        final Room room2 = new Room(factory, new RectangularShape( 120, 22, 75, 75 ), "Production Room 2");
        new Door(room2, Room.WALL.LEFT, 10, 20, true, "Entrance");
        final Area area2 = new Area(room2, new RectangularShape( 135, 35, 50, 50 ), "Production Area 1");
        final Machine machine2 = new Machine(area2, new RectangularShape( 150, 50, 15, 15 ), "Machine 1");

        final int baselineSize = 3;
        final int xCoordinate = 10;
        final int yCoordinate = 165;
        final int width =  10;
        final int height = 30;
        final BasicPolygonShape conveyorShape = new BasicPolygonShape();
        conveyorShape.addVertex(new BasicVertex(xCoordinate, yCoordinate));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width, yCoordinate));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new BasicVertex(xCoordinate + width + baselineSize, yCoordinate + height));
        conveyorShape.addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height));
        conveyorShape.addVertex(new BasicVertex(xCoordinate - baselineSize, yCoordinate + height - baselineSize));
        conveyorShape.addVertex(new BasicVertex(xCoordinate, yCoordinate + height - baselineSize));

        final Room chargingRoom = new Room(factory, new RectangularShape(125, 125, 50, 50), "Charging Room");
        new Door(chargingRoom, Room.WALL.RIGHT, 10, 20, false, "Entrance");
        final ChargingStation chargingStation = new ChargingStation(factory, new RectangularShape(150, 145, 15, 15), "Charging Station");

        final FactoryPathFinder jgraphPahtFinder = new JGraphTDijkstraFactoryPathFinder(factory, 5);
        final Robot robot1 = new Robot(factory, jgraphPahtFinder, new CircularShape(5, 5, 2), new Battery(10), "Robot 1");
        robot1.addTargetComponent(machine1);
        robot1.addTargetComponent(machine2);
        //robot1.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));
        //robot1.addTargetComponent(chargingStation);

        final FactoryPathFinder customPathFinder = new CustomDijkstraFactoryPathFinder(factory, 5);
        final Robot robot2 = new Robot(factory, customPathFinder, new CircularShape(45, 5, 2), new Battery(10), "Robot 2");
        //robot2.addTargetComponent(chargingStation);
        robot2.addTargetComponent(machine1);
        robot2.addTargetComponent(machine2);

        setRemoteCanvas(factory);
        //startAnimation();
        //setRemoteCanvas(getFactory());
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addObserver(final Observer observer) {
        if (factoryModel != null) {
            System.out.println("Added observer");
            return factoryModel.addObserver(observer);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeObserver(final Observer observer) {
        if (factoryModel != null) {
            return factoryModel.removeObserver(observer);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCanvas(final Canvas canvasModel) {
        factoryModel = (Factory) canvasModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Canvas getCanvas() {
        return factoryModel;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startAnimation() {
        try {
            if(animationThread!=null && !animationThread.isInterrupted())
                animationThread.interrupt();
            HttpResponse<String> startedSimulating = httpClient.send(startRequest, HttpResponse.BodyHandlers.ofString());
            if(!Boolean.parseBoolean(startedSimulating.body())){
                System.out.println(startedSimulating.body());
                System.out.println("Web server could not start simulation");
                return;
            }
            System.out.println("Web server started the simulation");
            setRemoteCanvas(getFactory()); //update for the first time
            animationThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        updateViewer();
                    } catch (InterruptedException ignored) {}
                }
            });
            animationThread.start();
        } catch (Exception e) {
            System.out.println("Error starting animation: "+e.getLocalizedMessage());
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopAnimation() {
        if(!animationThread.isInterrupted()) {
            animationThread.interrupt();
            try {
                httpClient.send(stopRequest, HttpResponse.BodyHandlers.ofString());
            } catch (Exception e) {
                System.out.println("Error stopping animation: " + e.getLocalizedMessage());
            }
            factoryModel.simulationStarted = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAnimationRunning() {
        //return true;
        //return factoryModel != null;
        return factoryModel != null && factoryModel.isSimulationStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CanvasPersistenceManager getPersistenceManager() {
        //return null;
        return persistenceManager;
    }

    private Factory getFactory() {
        try {
            HttpResponse<String> response = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            System.out.println("Reiceved factory: " + response.body());
            Factory factory = objectMapper.readValue(response.body(), Factory.class);
            factory.simulationStarted = true;
            return factory;
        } catch (Exception e) {
            System.out.println("Error retrieving factory: " + e.getLocalizedMessage());
        }
        return null;
    }

    private void updateViewer() throws InterruptedException {
        while (true) {
            if(!(getCanvas() instanceof Factory factory)) {
                System.out.println("Canvas was not a factory");
                break;
            }
            /*if(!factory.isSimulationStarted()) {
                System.out.println("Simulation stopped");
                break;
            }*/
            final Factory remoteFactoryModel = getFactory();
            this.setRemoteCanvas(remoteFactoryModel);
            Thread.sleep(1000);
        }
    }

    public synchronized void setRemoteCanvas(final Factory newFactory) {
        Canvas currentCanvas = getCanvas();
        if(currentCanvas instanceof Factory currentFactory) {
            final List<Observer> observers = currentFactory.getObservers();
            setCanvas(newFactory);
            for (final Observer observer : observers) {
                newFactory.addObserver(observer);
            }
            System.out.println("Notifying " + newFactory.getObservers().size() + " observers");
            newFactory.notifyObservers();
        } else if (currentCanvas == null) {
            setCanvas(newFactory);
        }
    }
}
