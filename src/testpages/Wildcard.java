package testpages;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;

import router.Page;
import router.Route;

@Route(path = "*")
public class Wildcard implements Page {
    private HttpExchange exchange;

    public Wildcard(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void servePage() {
        String response = "404 - Not Found";
        try {
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("404: " + exchange.getRequestURI().toString());
    }
    
}
