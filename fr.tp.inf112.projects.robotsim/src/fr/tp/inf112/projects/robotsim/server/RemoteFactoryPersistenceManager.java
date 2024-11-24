package fr.tp.inf112.projects.robotsim.server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.io.File;
import java.io.IOException;

import fr.tp.inf112.projects.canvas.model.Canvas;
import fr.tp.inf112.projects.canvas.model.CanvasChooser;
import fr.tp.inf112.projects.canvas.model.impl.AbstractCanvasPersistenceManager;

public class RemoteFactoryPersistenceManager extends AbstractCanvasPersistenceManager {
    
    private Socket socket;

    public RemoteFactoryPersistenceManager(CanvasChooser canvaschooser,int serverPort) {
    	super(canvaschooser);
    	Socket socket1;
		try {
			socket1 = new Socket("localhost", serverPort);
	    	socket= socket1;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	
    }

    // Méthode pour persister un objet Factory à distance
    @Override
    public void persist(Canvas canvas) throws IOException {
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             
            // Envoi de l'objet Factory au serveur pour persistance
            out.writeObject(canvas);
            out.flush();
    }
    @Override
    // Méthode pour lire un modèle de Factory à distance
    public Canvas read(String canvasId) throws IOException {
        try {
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             
            // Envoi du nom de la fabrique (factoryName) pour lecture
            out.writeObject(canvasId);
            out.flush();
            
            // Lecture de la fabrique envoyée par le serveur

            Object receivedObject = in.readObject();

            if (receivedObject instanceof Canvas) {
                return (Canvas) receivedObject;

            } else {
                throw new IOException("Unexpected object received from server.");
            }}
            catch (Exception e) {
            	throw new IOException("Unexpected object received from server.");
            }
    }
	@Override
	public boolean delete(final Canvas canvasModel)
	throws IOException {
		final File canvasFile = new File(canvasModel.getId());
		
		return canvasFile.delete();
	
    }
}
