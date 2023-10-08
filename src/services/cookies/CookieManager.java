package services.cookies;

import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;

public class CookieManager implements ICookieManager {
    private HttpExchange exchange;

    public CookieManager(HttpExchange exchange) {
        this.exchange = exchange;
    }

    @Override
    public long getSessionId() {
        String cookieVal = getCookieValue("sessionId");
        if (cookieVal == null) return -1;

        try {
            return Long.parseLong(cookieVal);
        } catch (NumberFormatException e) {
            System.out.println("Couldn't format sessionId to long.");
            return -1;
        }
    }

    @Override
    public String getCookieValue(String name) {
        String cookies = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookies == null) return null; 
        
        for (String cookie : cookies.split("; ")) {
            String[] cookieNameValuePair = cookie.split("=");
            if (cookieNameValuePair.length == 2 && cookieNameValuePair[0].equals(name)) {
                return cookieNameValuePair[1];
            }
        }

        return null;
    }

    @Override
    public void setCookie(String name, Object obj, int maxAge) {
        List<String> cookies = exchange.getResponseHeaders().get("Set-Cookie");
        if (cookies == null) {
            cookies = new ArrayList<>();
            exchange.getResponseHeaders().put("Set-Cookie", cookies);
        }
        String cookieValue = name + "=" + obj.toString() + "; Max-Age=" + maxAge;
        cookies.add(cookieValue);
    }

    @Override
    public void setCookie(String name, Object obj) {
        List<String> cookies = exchange.getResponseHeaders().get("Set-Cookie");
        if (cookies == null) {
            cookies = new ArrayList<>();
            exchange.getResponseHeaders().put("Set-Cookie", cookies);
        }
        String cookieValue = name + "=" + obj.toString();
        cookies.add(cookieValue);
    }
}
