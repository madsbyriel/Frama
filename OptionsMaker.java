import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Stack;

public class OptionsMaker {
    public static void main(String[] args) {
        String optionsText = "-d out\n";

        String cwd = System.getProperty("user.dir");
        String rootDirPath = cwd + "/src";

        File rootDir = new File(rootDirPath);
        Stack<File> dirStack = new Stack<>();
        dirStack.push(rootDir);
        while (dirStack.size() > 0) {
            File dir = dirStack.pop();
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    dirStack.push(file);
                    continue;
                }
                String fp = file.getPath();
                if (file.getPath().endsWith(".java")) {
                    optionsText += fp.substring(cwd.length() + 1, fp.length()).replace("\\", "/") + "\n";
                }
            }
        }

        try (FileOutputStream optionsOutputStream = new FileOutputStream(cwd + "/options", false)) {
            optionsOutputStream.write(optionsText.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
