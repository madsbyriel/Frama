package server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import router.Page;
import router.Router;
import service_provider.IServiceProvider;
import service_provider.ServiceProvider2;
import services.cookies.CookieManager;
import services.cookies.ICookieManager;

public class Server {
    private HttpServer server;
    private IServiceProvider serviceProvider;
    
    public Server(String hostname, int port, int backlog) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
        this.serviceProvider = new ServiceProvider2();
    }

    public void startServer() {
        this.server.start();
        server.createContext("/", exchange -> routeHandler(exchange));
    }

    private void routeHandler(HttpExchange exchange) {
        try {
            IServiceProvider pageServiceProvider = new ServiceProvider2();
            pageServiceProvider.addTransientService(IServiceProvider.class, ServiceProvider2.class);
            pageServiceProvider.addTransientService(ICookieManager.class, CookieManager.class);
            Page page = Router.getPage(pageServiceProvider, exchange.getRequestURI().getPath());
            page.servePage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
