package services.cookies;

import com.sun.net.httpserver.HttpExchange;

public interface ICookieManager {
    long getSessionId(HttpExchange exchange);
    String getCookieValue(HttpExchange exchange, String name);
    void setCookie(HttpExchange exchange, String name, Object obj, int maxAge);
    void setCookie(HttpExchange exchange, String name, Object obj);
}
