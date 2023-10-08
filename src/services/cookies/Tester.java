package services.cookies;

public class Tester implements ITester {
    private ICookieManager cookieManager;

    public Tester(ICookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    public void show() {
        System.out.println("HEY!");
    }
}
