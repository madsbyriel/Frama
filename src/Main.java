import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;

import router.Router;
import server.Server;

public class Main {
    private static Map<String, Object> cookies = new HashMap<>();

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
        String cookie = exchange.getRequestHeaders().getFirst("Cookie");
        
        if (cookie == null) {
            HttpCookie newCookie = new HttpCookie("Test", "Value");
            newCookie.setMaxAge(3600);
            exchange.getResponseHeaders().add("Set-Cookie", newCookie.toString());
            System.out.println(newCookie.toString());
        }
        else {
            System.out.println(cookie);
        }

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
