package fr.tp.inf112.projects.robotsim.app;

import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.robotsim.model.Area;
import fr.tp.inf112.projects.robotsim.model.Battery;
import fr.tp.inf112.projects.robotsim.model.ChargingStation;
import fr.tp.inf112.projects.robotsim.model.Door;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.Machine;
import fr.tp.inf112.projects.robotsim.model.Robot;
import fr.tp.inf112.projects.robotsim.model.Room;
import fr.tp.inf112.projects.robotsim.model.path.CustomDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.robotsim.service.RemoteSimulatorController;


public class SimulatorApplication {

    private static final Logger logger = Logger.getLogger(SimulatorApplication.class.getName());

    public static void main(String[] args) {
        Logger logger = Logger.getLogger(SimulatorApplication.class.getName());
        logger.info("Starting the distributed robot simulator with GUI...");

        // Création d'une Factory complète
        final Factory factory = new Factory(200, 200, "Simple Test Puck Factory");
        final Room room1 = new Room(factory, new RectangularShape(20, 20, 75, 75), "Production Room 1");
        new Door(room1, Room.WALL.BOTTOM, 10, 20, true, "Entrance");
        final Area area1 = new Area(room1, new RectangularShape(35, 35, 50, 50), "Production Area 1");
        final Machine machine1 = new Machine(area1, new RectangularShape(50, 50, 15, 15), "Machine 1");

        final Room room2 = new Room(factory, new RectangularShape(120, 22, 75, 75), "Production Room 2");
        new Door(room2, Room.WALL.LEFT, 10, 20, true, "Entrance");
        final Area area2 = new Area(room2, new RectangularShape(135, 35, 50, 50), "Production Area 1");
        final Machine machine2 = new Machine(area2, new RectangularShape(150, 50, 15, 15), "Machine 1");

        final int baselineSize = 3;
        final int xCoordinate = 10;
        final int yCoordinate = 165;
        final int width = 10;
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

        final FactoryPathFinder customPathFinder = new CustomDijkstraFactoryPathFinder(factory, 5);
        final Robot robot2 = new Robot(factory, customPathFinder, new CircularShape(45, 5, 2), new Battery(10), "Robot 2");
        robot2.addTargetComponent(machine1);
        robot2.addTargetComponent(machine2);

        // Création du contrôleur distant et initialisation avec la Factory
        RemoteSimulatorController remoteSimulatorController = new RemoteSimulatorController("localhost");
        factory.setId("/home/bob/eclipse-workspace/fr.tp.inf112.projects.robotsim/resources/default.factory");
        remoteSimulatorController.setCanvas(factory);



        SwingUtilities.invokeLater(() -> {
            final CanvasViewer canvasViewer = new CanvasViewer(remoteSimulatorController); 
            logger.info("Initializing Canvas viewer...");
            if (canvasViewer instanceof JFrame) {
                ((JFrame) canvasViewer).setVisible(true);
            } else {
                JFrame frame = new JFrame("Robot Simulator Viewer");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(800, 600);
                frame.getContentPane().add(canvasViewer); // Ajoutez le composant à la fenêtre
                frame.setVisible(true);
            }
            logger.info("Canvas viewer initialized.");
        });



    }

}
