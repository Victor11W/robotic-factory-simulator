package fr.tp.inf112.projects.robotsim.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class FactoryServer {
    private static final Logger logger = Logger.getLogger(FactoryServer.class.getName());
    public static void main(String[] args) {
        new FactoryServer().run();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            logger.info("Server started");
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    logger.info("Client connected");
                    Thread thread = new Thread(new FactoryHostServer(socket));
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
