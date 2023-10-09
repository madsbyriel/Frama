package server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import router.Page;
import router.Router;
import services.cookies.CookieManager;
import services.cookies.ICookieManager;
import services.service_provider.IServiceProvider;
import services.service_provider.ServiceProvider2;

public class Server {
    private HttpServer server;
    
    public Server(String hostname, int port, int backlog) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
    }

    public void startServer() {
        this.server.start();
        server.createContext("/", exchange -> routeHandler(exchange));
    }

    private void routeHandler(HttpExchange exchange) {
        IServiceProvider pageServiceProvider = ServiceProvider2.getServiceProvider(exchange);

        pageServiceProvider.clearTransientObjects();

        // Add services to the page.
        pageServiceProvider.addTransientService(HttpExchange.class, HttpExchange.class, exchange);
        pageServiceProvider.addTransientService(IServiceProvider.class, ServiceProvider2.class, pageServiceProvider);
        pageServiceProvider.addTransientService(ICookieManager.class, CookieManager.class);


        try {
            IServiceProvider pageServiceProvider = new ServiceProvider2();
            pageServiceProvider.addTransientService(HttpExchange.class, HttpExchange.class, exchange);
            pageServiceProvider.addTransientService(IServiceProvider.class, ServiceProvider2.class);
            pageServiceProvider.addTransientService(ICookieManager.class, CookieManager.class);
            Page page = Router.getPage(pageServiceProvider, exchange.getRequestURI().getPath());
            page.servePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
