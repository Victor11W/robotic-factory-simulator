package fr.tp.inf112.projects.robotsim.server;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.ServerSocket;
import java.net.Socket;


import fr.tp.inf112.projects.canvas.view.FileCanvasChooser;
import fr.tp.inf112.projects.robotsim.model.Factory;
import fr.tp.inf112.projects.robotsim.model.FactoryPersistenceManager;

import java.io.IOException;
// à vérifier car pas de vérificaiton pour le moment ont été faites
public class HostServerRobot implements Runnable {
	private Socket socket;
	public HostServerRobot(Socket socket) {
		this.socket = socket;
	}
	public static void main(String[] args) {
        int port = 8080; // Choisir un port sur lequel écouter

        // Créer un ServerSocket pour écouter les connexions
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Le serveur est en attente de connexions sur le port " + port);

            // Boucle infinie pour accepter les connexions des clients
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accepter une nouvelle connexion
                System.out.println("Un client s'est connecté");

                // Créer une nouvelle instance de HostServer avec la socket client
                HostServerRobot hostServer = new HostServerRobot(clientSocket);

                // Exécuter la gestion du client dans un nouveau thread
                Thread thread = new Thread(hostServer);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void run() {
		try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) 
		
	{   Object receivedObject = in.readObject();
		if (receivedObject instanceof String receivedString) {
            FileCanvasChooser chooser = new FileCanvasChooser("txt", "Texte");
            FactoryPersistenceManager persistenceManager = new FactoryPersistenceManager(chooser);
            persistenceManager.read(receivedString);
		}
		if(receivedObject instanceof Factory factory) {
            FileCanvasChooser chooser = new FileCanvasChooser("txt", "Texte");
            FactoryPersistenceManager persistenceManager = new FactoryPersistenceManager(chooser);
            persistenceManager.persist(factory);
			
		}}
		catch (IOException | ClassNotFoundException e) {
            // Gérer les erreurs liées aux entrées/sorties
            e.printStackTrace();
        } finally {
            // Fermer la socket pour libérer les ressources
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
