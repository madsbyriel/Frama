package pages;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

import router.Page;
import router.Route;
import services.Context;
import services.testservices.Scop;
import services.testservices.Stat;
import services.testservices.Trans;

@Route( path = "/debug")
public class DebugPage implements Page {
    private HttpExchange exchange;
    private Stat stat;
    private Trans trans;
    private Scop scop;

    public DebugPage(Context context, Stat stat, Trans trans, Scop scop) {
        this.stat = stat;
        this.trans = trans;
        this.scop = scop;
        this.exchange = context.getExchange();
    }

    @Override
    public void servePage() {
        URI uri = exchange.getRequestURI();
        
        List<Object> debugList = new ArrayList<>();
        debugList.add("PATH: " + uri.getPath());
        debugList.add("QUERY: " + uri.getQuery());
        debugList.add("Stat object address: " + stat);
        debugList.add("Trans object address: " + trans);
        debugList.add("Scop object address: " + scop);

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
    }
}
