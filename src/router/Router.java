package router;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Router {
    private static Map<String, Class<?>> routeClasses;

    public static void initializeRouter() {
        Class<?>[] classes = classPathsToClasses(getAllClassPaths());
        classes = filterByClassAnnotation(classes, Route.class);

        Router.routeClasses = new HashMap<>(); 
        for (Class<?> c : classes) {
            Route routeAnnotation = (Route)c.getAnnotation(Route.class);
            Router.routeClasses.put(routeAnnotation.path(), c);
        }
    }

    private static String[] getAllClassPaths() {
        List<String> classPaths = new ArrayList<>();
        String cwd = System.getProperty("user.dir");
        String cp = "\\out";
        Stack<File> dirStack = new Stack<>();
        dirStack.push(new File(cwd + cp));
        while (dirStack.size() > 0)  {
            File dir = dirStack.pop();
            for (File file : dir.listFiles()) {
                if (file.isDirectory()) {
                    dirStack.push(file);
                    continue;
                }
                String fp = file.getPath();
                String classPath = fp.substring(cwd.length() + cp.length() + 1, fp.length() - 6).replace("\\", ".");
                classPaths.add(classPath);
            }
        }
        
        return classPaths.toArray(new String[0]);
    }

    private static Class<?>[] classPathsToClasses(String[] classPaths)  {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String cp : classPaths) {
            try {
                classes.add(classLoader.loadClass(cp));
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't add class at class path: " + cp);
            }
        }
        return classes.toArray(new Class<?>[0]);
    }

    private static <T extends Annotation> Class<?>[] filterByClassAnnotation(Class<?>[] classes, Class<T> annotationType) {
        List<Class<?>> filteredClasses = new ArrayList<>();
        for (Class<?> c : classes) {
            if (c.getAnnotation(annotationType) != null) {
                filteredClasses.add(c);
            }
        }
        return filteredClasses.toArray(new Class<?>[0]);
    }
}
