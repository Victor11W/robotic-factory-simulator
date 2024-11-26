package fr.tp.inf112.projects.robotsim.server;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

import java.io.*;
import java.net.Socket;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {
    private final Socket socket;
    private final ObjectOutputStream socketObjOutStream;
    private final ObjectInputStream socketObjInStream;
    public RemoteFactoryPersistenceManager(final CanvasChooser canvasChooser, Socket socket) throws IOException {
        super(canvasChooser);
        this.socket = socket;
        this.socketObjOutStream = new ObjectOutputStream(socket.getOutputStream());
        this.socketObjInStream = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Canvas read(final String canvasId) throws IOException {
        try {
            socketObjOutStream.writeObject(canvasId);
            socketObjOutStream.flush();

            Object object = socketObjInStream.readObject();
            if (object instanceof Canvas) {
                return (Canvas) object;
            } else {
                throw new IOException("Received object is not a Canvas");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persist(Canvas canvasModel) throws IOException {
        socketObjOutStream.writeObject(canvasModel);
        socketObjOutStream.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(final Canvas canvasModel)
            throws IOException {
        final File canvasFile = new File(canvasModel.getId());

        return canvasFile.delete();
    }
}
