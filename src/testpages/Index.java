package testpages;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import router.Page;
import router.Route;
import services.cookies.ICookieManager;

@Route(path = "/")
public class Index implements Page {
    private HttpExchange exchange;
    private ICookieManager cookieManager;

    public Index(HttpExchange exchange, ICookieManager cookieManager) {
        this.exchange = exchange;
        this.cookieManager = cookieManager;
    }

    @Override
    public void servePage() {
        String response = "Index " + cookieManager.getSessionId();
        
        try {
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        exchange.close();
    }
    
}
