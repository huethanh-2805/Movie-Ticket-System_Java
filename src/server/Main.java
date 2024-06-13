package server;

import client.Client;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static void main(String[] args) {
        // TODO code application logic here
        new Thread(() -> {
            Server server = null;
            try {
                server = new Server();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
            server.start();
        }).start();

        // Start Client in a new thread
        new Thread(() -> {
            Client client = new Client();
            client.start();
        }).start();
    }
    
}
