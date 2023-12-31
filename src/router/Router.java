package router;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import services.service_provider.IServiceProvider;

public class Router {
    private static Map<String, Class<? extends IPage>> pageClasses;

    public static void initializeRouter() {
        List<Class<?>> classes = classPathsToClasses(getAllClassPaths());
        List<Class<? extends IPage>> filteredClasses = filterByAnnotationAndInterface(classes, IPage.class, Route.class);

        Router.pageClasses = new HashMap<>(); 
        for (Class<? extends IPage> c : filteredClasses) {
            Route routeAnnotation = (Route)c.getAnnotation(Route.class);
            Router.pageClasses.put(routeAnnotation.path(), c);
        }
    }

    public static IPage getPage(IServiceProvider provider, String route) throws Exception {
        Class<? extends IPage> pageClass = pageClasses.get(route);
        
        if (pageClass == null) pageClass = pageClasses.get("*");

        IPage page = provider.createObjectFromServices(pageClass);

        return page;
    }

    private static List<String> getAllClassPaths() {
        List<String> classPaths = new ArrayList<>();
        String cwd = System.getProperty("user.dir");
        String cp = "/out";
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
                classPath = classPath.replace("/", ".");
                classPaths.add(classPath);
            }
        }
        
        return classPaths;
    }

    private static List<Class<?>> classPathsToClasses(List<String> classPaths)  {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        for (String cp : classPaths) {
            try {
                classes.add(classLoader.loadClass(cp));
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't add class at class path: " + cp);
            }
        }

        return classes;
    }

    private static <T, A extends Annotation> List<Class<? extends T>> filterByAnnotation(List<Class<? extends T>> classes, Class<A> annotationType) {
        List<Class<? extends T>> filteredClasses = new ArrayList<>();
        for (Class<? extends T> c : classes) {
            if (c.getAnnotation(annotationType) != null) {
                filteredClasses.add(c);
            }
        }
        
        return filteredClasses;
    }

    @SuppressWarnings("unchecked")
    private static <T> List<Class<? extends T>> filterByInterface(List<Class<?>> classes, Class<T> interfaze) {
        List<Class<? extends T>> classesImplementingInterfaze = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (interfaze.isAssignableFrom(clazz)) {
                classesImplementingInterfaze.add((Class<? extends T>) clazz);
            }
        }

        return classesImplementingInterfaze;
    }

    private static <T, A extends Annotation> List<Class<? extends T>> filterByAnnotationAndInterface(List<Class<?>> classes, Class<T> interfaze, Class<A> annotation) {
        List<Class<? extends T>> filteredClasses = filterByInterface(classes, interfaze);
        filteredClasses = filterByAnnotation(filteredClasses, annotation);
        return filteredClasses;
    }
}
