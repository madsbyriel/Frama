package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import router.Page;
import router.Router;
import service_provider.ServiceProvider;

public class Server {
    private HttpServer server;
    
    public Server(String hostname, int port, int backlog) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
    }

    public void startServer() {
        server.createContext("/", exchange -> routeHandler(exchange));
        this.server.start();
    }

    private void routeHandler(HttpExchange exchange) {
        ServiceProvider serviceProvider = ServiceProvider.getScopedServiceProvider(exchange);
        Page p = null;
        try {
            p = Router.getPage(serviceProvider, exchange.getRequestURI().getPath());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        p.servePage();
    }

    private void debugResponse(HttpExchange exchange) throws IOException  {
        URI uri = exchange.getRequestURI();
        
        List<Object> debugList = new ArrayList<>();
        debugList.add("PATH: " + uri.getPath());
        debugList.add("QUERY: " + uri.getQuery());

        String response = "";
        for (Object obj : debugList) {
            response += obj.toString() + "\n";
        }

        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }

    public interface OnRequestReceivedDel {
        void onRequestReceived(HttpExchange exchange) throws IOException;
    }
}
