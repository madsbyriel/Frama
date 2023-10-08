import java.io.IOException;

import router.Router;
import server.Server;

public class Main {
    public static void main(String[] args) {
        Router.initializeRouter();

        Server server = null;
        try {
            server = new Server("localhost", 31415, 20);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server.startServer();
    }
}
