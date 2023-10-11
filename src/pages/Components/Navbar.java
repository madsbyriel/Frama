package pages.Components;

import pages.BasePage;
import router.Route;
import services.Context;

@Route(path = "/navbar")
public class Navbar extends BasePage {

    public Navbar(Context context) {
        super(context);
    }

    @Override
    public void servePage() {
        sendFileAndHeaders("wwwRoot\\components\\navbar.html");
    }
    
}
