import java.io.IOException;
import java.net.URI;

import com.sun.net.httpserver.HttpExchange;

import server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = null;
        try {
            server = new Server("localhost", 31415, 20);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        server.startServer(); 
        server.onRequestReceived(exchange -> routeHandler(exchange));
    }

    private static void routeHandler(HttpExchange exchange) throws IOException {
        basicResponse(exchange);
    }

    private static void basicResponse(HttpExchange exchange) throws IOException  {
        URI uri = exchange.getRequestURI();
        System.out.println(uri.toString());
        
        String response = "PATH: " + uri.getPath() + "\n" 
            + "QUERY: " + uri.getQuery() + "\n";
        
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
