package pages;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import router.Page;
import router.Route;
import services.Context;

@Route(path = "/")
public class Index implements Page {
    private HttpExchange exchange;

    public Index(Context context) {
        this.exchange = context.getExchange();
    }

    @Override
    public void servePage() {
        String response = "Index page!";
        
        try {
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
}
