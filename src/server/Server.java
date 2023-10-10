package server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import router.Page;
import router.Router;
import services.Context;
import services.service_provider.IServiceProvider;
import services.service_provider.ServiceProvider3;

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
        IServiceProvider serviceProvider = ServiceProvider3.getServiceProvider(exchange);
        
        serviceProvider.clearTransientObjects();
        Context context = serviceProvider.getService(Context.class);
        context.setExchange(exchange);

        try {
            Page page = Router.getPage(serviceProvider, exchange.getRequestURI().getPath());
            if (page == null) {
                System.out.println("Couldn't create page at route: " + exchange.getRequestURI().getPath());
                return;
            }
            page.servePage();
        } catch (Exception e) {
            e.printStackTrace();
        }

        exchange.close();
    }
}
