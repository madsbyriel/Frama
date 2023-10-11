package pages.Components;

import pages.BasePage;
import router.Route;
import services.Context;

@Route(path = "/head")
public class Head extends BasePage {

    public Head(Context context) {
        super(context);
    }

    @Override
    public void servePage() {
        sendFileAndHeaders("wwwRoot\\components\\head.html");
    }
    
}
