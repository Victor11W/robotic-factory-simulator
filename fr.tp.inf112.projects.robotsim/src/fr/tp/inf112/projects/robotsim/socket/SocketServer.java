package fr.tp.inf112.projects.robotsim.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class SocketServer {
    private static final Logger logger = Logger.getLogger(SocketServer.class.getName());
    public static void main(String[] args) {
        new SocketServer().run();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            logger.info("Server started");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    logger.info("Client connected");
                    Thread thread = new Thread(new SocketHost(socket));
                    thread.start();
                } catch (Exception ex) {
                    logger.warning(ex.getLocalizedMessage());
                }
            }
        } catch (Exception ex) {
            logger.warning(ex.getLocalizedMessage());
        }
    }

}
