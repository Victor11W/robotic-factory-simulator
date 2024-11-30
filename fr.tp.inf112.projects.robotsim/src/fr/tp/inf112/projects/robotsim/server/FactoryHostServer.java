package fr.tp.inf112.projects.robotsim.server;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

import java.io.*;
import java.net.Socket;
import java.util.logging.Logger;

public class FactoryHostServer implements Runnable {
    private static final Logger logger = Logger.getLogger(FactoryHostServer.class.getName());
    private final ObjectOutputStream socketObjOutStream;
    private final ObjectInputStream socketObjInStream;
    private final FactoryPersistenceManager fpManager;

    public FactoryHostServer(Socket socket) throws IOException {
        this.socketObjOutStream = new ObjectOutputStream(socket.getOutputStream());
        this.socketObjInStream = new ObjectInputStream(socket.getInputStream());
        final FileCanvasChooser canvasChooser = new FileCanvasChooser("factory", "Puck Factory");
        this.fpManager = new FactoryPersistenceManager(canvasChooser);
    }

    @Override
    public void run() {
        logger.info("Thread running");
        try {
            while (true) {
                Object receivedObject = socketObjInStream.readObject();
                if (receivedObject instanceof Factory factory) {
                    fpManager.persist(factory);
                } else if (receivedObject instanceof String filename) {
                    Canvas canvas = fpManager.read(filename);
                    socketObjOutStream.writeObject(canvas);
                    socketObjOutStream.flush();
                }
            }
        }catch (EOFException ex) {
            logger.info("Client disconnected");
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.warning(ex.getLocalizedMessage());
        } finally {
        try {
            socketObjInStream.close();
            socketObjOutStream.close();
        } catch (IOException e) {
            logger.warning("Error closing streams: " + e.getMessage());
        }
    }
    }
}
