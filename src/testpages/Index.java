package testpages;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import router.Page;
import router.Route;

@Route(path = "/")
public class Index implements Page {
    private HttpExchange exchange;

    public Index(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public void servePage() {
        String response = "Index";
        
        try {
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        exchange.close();
    }
    
}
