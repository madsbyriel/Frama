import java.io.IOException;

import router.Router;
import server.Server;
import service_provider.IServiceProvider;
import service_provider.ServiceProvider3;
import services.cookies.ITester;
import services.cookies.Tester;

public class Main {
    public static void main(String[] args) {
        IServiceProvider serviceProvider  = ServiceProvider3.getServiceProvider(0l);
        serviceProvider.addStaticService(ITester.class, Tester.class);
        ITester tester = serviceProvider.getService(ITester.class);

        System.out.println(tester);
        /*Router.initializeRouter();

        Server server = null;
        try {
            server = new Server("localhost", 31415, 20);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server.startServer();
        */
    }
}
