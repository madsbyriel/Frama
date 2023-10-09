package testpages;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import router.Page;
import router.Route;
import services.service_provider.IServiceProvider;

@Route( path = "/debug")
public class DebugPage implements Page {
    private HttpExchange exchange;
    private IServiceProvider serviceProvider;

    public DebugPage(HttpExchange exchange, IServiceProvider serviceProvider) {
        this.exchange = exchange;
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void servePage() {
        URI uri = exchange.getRequestURI();
        
        List<Object> debugList = new ArrayList<>();
        debugList.add("PATH: " + uri.getPath());
        debugList.add("QUERY: " + uri.getQuery());
        debugList.add("SessionId: " + serviceProvider.getSessionId());
        debugList.add("Service Provider: " + serviceProvider);


        String response = "";
        for (Object obj : debugList) {
            response += obj.toString() + "\n";
        }

        try {
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        exchange.close();
    }
}
