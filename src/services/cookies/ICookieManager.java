package services.cookies;

public interface ICookieManager {
    long getSessionId();
    String getCookieValue(String name);
    void setCookie(String name, Object obj, int maxAge);
    void setCookie(String name, Object obj);
}
