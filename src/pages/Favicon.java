package pages;

import router.Route;
import services.Context;

@Route(path = "/favicon.ico")
public class Favicon extends BasePage {

    public Favicon(Context context) {
        super(context);
    }

    @Override
    public void servePage() {
        sendFileAndHeaders("wwwRoot\\favicon.ico");
    }
    
}
