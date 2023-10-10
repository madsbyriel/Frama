import java.io.IOException;

import router.Router;
import server.Server;
import services.Context;
import services.cookies.CookieManager;
import services.cookies.ICookieManager;
import services.service_provider.IServiceProvider;
import services.service_provider.ServiceProvider3;
import services.testservices.Scop;
import services.testservices.Stat;
import services.testservices.Trans;

public class Main {
    public static void main(String[] args) {
        IServiceProvider serviceProvider = ServiceProvider3.getEmptyService();
        serviceProvider.addTransientService(Context.class, Context.class);
        serviceProvider.addTransientService(ICookieManager.class, CookieManager.class);

        // Testing purposes:
        serviceProvider.addScopedService(Scop.class, Scop.class);
        serviceProvider.addStaticService(Stat.class, Stat.class);
        serviceProvider.addTransientService(Trans.class, Trans.class);


        Router.initializeRouter();

        Server server = null;
        try {
            server = new Server("192.168.1.243", 31415, 20);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server.startServer();
    }
}
