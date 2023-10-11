package pages;

import services.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

import router.IPage;

public abstract class BasePage implements IPage {
    protected HttpExchange exchange;

    public BasePage(Context context) {
        this.exchange = context.getExchange();
    }

    protected void sendFileAndHeaders(String relativeFilePath) {
        String basePath = System.getProperty("user.dir");
        basePath += "/" + relativeFilePath.replace("\\", "/");
        File file = new File(basePath);

        try {
            exchange.sendResponseHeaders(200, file.length());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (InputStream iS = new FileInputStream(file)) {
            OutputStream oS = exchange.getResponseBody();
            byte[] buffer = new byte[2048];
            while (iS.available() > 0) {
                int readBytes = iS.read(buffer);
                oS.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
