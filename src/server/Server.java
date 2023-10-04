package server;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {
    private HttpServer server;
    
    public Server(String hostname, int port, int backlog) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(hostname, port), backlog);
    }

    public void startServer() {
        this.server.start();
    }

    public void onRequestReceived(OnRequestReceivedDel callback) {
        this.server.createContext("/", exchange -> callback.onRequestReceived(exchange));
    }

    public interface OnRequestReceivedDel {
        void onRequestReceived(HttpExchange exchange) throws IOException;
    }
}
