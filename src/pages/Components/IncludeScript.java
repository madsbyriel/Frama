package pages.Components;

import pages.BasePage;
import router.Route;
import services.Context;

@Route(path = "/includescript.js")
public class IncludeScript extends BasePage {

    public IncludeScript(Context context) {
        super(context);
    }

    @Override
    public void servePage() {
        sendFileAndHeaders("wwwRoot\\includescript.js");
    }
    
}
