package fr.tp.inf112.projects.robotsim.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHost {

    public static void main(String[] args) {
        String serverHost = "localhost"; // L'adresse du serveur (ici, localhost)
        int port = 8080; // Le port sur lequel le serveur écoute

        try {
            // Se connecter au serveur
            Socket socket = new Socket(serverHost, port);
            System.out.println("Connecté au serveur " + serverHost + " sur le port " + port);

            // Créer les flux d'entrée et de sortie pour communiquer avec le serveur
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Envoyer un message au serveur
            String clientMessage = "Hello from the client!";
            out.println(clientMessage);
            System.out.println("Message envoyé au serveur : " + clientMessage);

            // Lire la réponse du serveur
            String serverResponse = in.readLine();
            System.out.println("Réponse du serveur : " + serverResponse);

            // Fermer la connexion
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
