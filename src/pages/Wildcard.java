package pages;

import router.Route;
import services.Context;

@Route(path = "*")
public class Wildcard extends BasePage {

    public Wildcard(Context context) {
        super(context);
    }

    @Override
    public void servePage() {
        sendFileAndHeaders("wwwRoot\\404.html");
    }
    
}
