import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import router.Page;
import router.Router;
import server.Server;
import service_provider.ServiceProvider;
import services.cookies.CookieManager;
import services.cookies.ICookieManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("Initializing router...");
        Router.initializeRouter();
        System.out.println("Done initializing router.");

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
