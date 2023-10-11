package pages;

import router.Route;
import services.Context;
import services.cookies.ICookieManager;

@Route(path = "/")
public class Index extends BasePage {
    public Index(Context context, ICookieManager cookieManager) {
        super(context);
    }

    @Override
    public void servePage() {
        sendFileAndHeaders("wwwRoot\\index.html");
    }
    
}
