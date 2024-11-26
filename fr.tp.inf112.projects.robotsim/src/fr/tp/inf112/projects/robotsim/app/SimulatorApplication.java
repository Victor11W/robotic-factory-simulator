package fr.tp.inf112.projects.robotsim.app;

import java.awt.Component;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.impl.BasicVertex;
import fr.tp.inf112.projects.canvas.view.CanvasViewer;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.*;
import fr.tp.inf112.projects.robotsim.model.path.CustomDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.FactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.path.JGraphTDijkstraFactoryPathFinder;
import fr.tp.inf112.projects.robotsim.model.shapes.BasicPolygonShape;
import fr.tp.inf112.projects.robotsim.model.shapes.CircularShape;
import fr.tp.inf112.projects.robotsim.model.shapes.PositionedShape;
import fr.tp.inf112.projects.robotsim.model.shapes.RectangularShape;
import fr.tp.inf112.projects.robotsim.server.RemoteFactoryPersistenceManager;
import com.fasterxml.jackson.core.util.BufferRecycler;
// cette partie du projet à été perdu peu de temps avant le rendu, j'ai récupéré une partie du projet d'axel vivenot pour reprendre une partie de ce que j'avais fait. (jusqu'au tp 2)
public class SimulatorApplication {
	public static final Logger logger = Logger.getLogger(SimulatorApplication.class.getName());

	public static void main(String[] args) {
		logger.info("Starting the robot simulator...");
		logger.config("With parameters " + Arrays.toString(args) + ".");
		
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
		//robot2.addTargetComponent(new Conveyor(factory, conveyorShape, "Conveyor 1"));
		
		SwingUtilities.invokeLater(new Runnable() {
			  
			@Override
	        public void run() {
				try  {
					//Monolithic application
					/*final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
					FactoryPersistenceManager fpManager = new FactoryPersistenceManager(canvasChooser);
					final Component factoryViewer = new CanvasViewer(new SimulatorController(factory, fpManager));
					canvasChooser.setViewer(factoryViewer);*/

					//Monolithic application with JSON model
					/*ObjectMapper objectMapper = new ObjectMapper();
					PolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
							.allowIfSubType(PositionedShape.class.getPackageName())

							.allowIfSubType(Area.class.getPackageName())
                            .allowIfSubType(Room.class.getPackageName())
                            .allowIfSubType(Factory.class.getPackageName())
							.allowIfSubType(Robot.class.getPackageName())

							.allowIfSubType(fr.tp.inf112.projects.robotsim.model.Component.class.getPackageName())
							.allowIfSubType(BasicVertex.class.getPackageName())
							.allowIfSubType(ArrayList.class.getName())
							.allowIfSubType(LinkedHashSet.class.getName())
							.build();
					objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
					final String jsonFactory = objectMapper.writeValueAsString(factory);
					System.out.println("jsonFactory: " + jsonFactory);
					final Factory fromJsonFactory = objectMapper.readValue(jsonFactory, Factory.class);
					System.out.println("factory size: " + fromJsonFactory.getWidth());
					final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
					FactoryPersistenceManager fpManager = new FactoryPersistenceManager(canvasChooser);
					final Component factoryViewer = new CanvasViewer(new SimulatorController(fromJsonFactory, fpManager));
					canvasChooser.setViewer(factoryViewer);*/


					//Monolithic application with persistence socket server, using a new factory model
					/*Socket socket = new Socket("localhost", 8080);
					final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
					RemoteFactoryPersistenceManager rfpManager = new RemoteFactoryPersistenceManager(canvasChooser, socket);
					final Component factoryViewer = new CanvasViewer(new SimulatorController(factory, rfpManager));
					canvasChooser.setViewer(factoryViewer);*/

					//Monolithic application with persistence socket server, using a remote stored model
					/*Socket socket = new Socket("localhost", 8080);
					final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
					RemoteFactoryPersistenceManager rfpManager = new RemoteFactoryPersistenceManager(canvasChooser, socket);
					Canvas canvas = rfpManager.read("/home/accel/telecom/slr/4sl01/f1.factory");
					final Component factoryViewer = new CanvasViewer(new SimulatorController((Factory) canvas, rfpManager));
					canvasChooser.setViewer(factoryViewer);*/

					//Distributed application with client, web-server and persistence socket server
					final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
					FactoryPersistenceManager fpManager = new FactoryPersistenceManager(canvasChooser);
					final Component factoryViewer = new CanvasViewer(new RemoteSimulatorController(fpManager,"/home/accel/telecom/slr/4sl01/f1.factory", "/home/accel/telecom/slr/f1.factory"));
					canvasChooser.setViewer(factoryViewer);

				} catch (Exception ex) {
					logger.warning(ex.getLocalizedMessage());
				}
			}
		});
	}
}
