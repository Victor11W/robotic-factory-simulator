package fr.tp.inf112.projects.robotsim.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;

public class HostServer implements Runnable {
	private Socket socket;
	public HostServer(Socket socket) {
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
                HostServer hostServer = new HostServer(clientSocket);

                // Exécuter la gestion du client dans un nouveau thread
                Thread thread = new Thread(hostServer);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void run() {
		try {
		InputStream inpStr = socket.getInputStream();
		Reader strReader = new InputStreamReader(inpStr);
		BufferedReader buffReader = new BufferedReader(strReader);
		String clientMessage = buffReader.readLine();
        System.out.println("Message reçu du client : " + clientMessage);
		// - read and decode input request
		OutputStream outStr = socket.getOutputStream();
		PrintWriter writer = new PrintWriter(outStr, true);// Autoflush
		String serverResponse = "I received: " + clientMessage + "!";
        writer.println(serverResponse);
        System.out.println("Réponse envoyée au client : " + serverResponse);
		}// - build and write response
		catch (IOException e) {
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
