package services;

import com.sun.net.httpserver.HttpExchange;

public class Context {
    private HttpExchange exchange;

    public HttpExchange getExchange() {
        return exchange;
    }

    public void setExchange(HttpExchange exchange) {
        this.exchange = exchange;
    }
}
