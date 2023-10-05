package service_provider;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private static Map<Class<?>, Object> staticServices;

    static {
        ServiceProvider.staticServices = new HashMap<>();
    }

    public static void addStaticService(Class<?> interfaze, Class<?> implementation) throws Exception  {
        Constructor<?>[] ctors = implementation.getConstructors();
        Constructor<?> ctor = getFirstConstructor(ctors);
        if (ctor == null) throw new IllegalArgumentException("Didn't find a constructor.");
        staticServices.put(interfaze, invokeConstructor(ctor));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> interfaze) throws Exception {
        Object object = staticServices.get(interfaze);
        if (object == null) throw new Exception("Couldn't find the service you are looking for. Perhaps it is not yet created, or added to the service provider");
        System.out.println(object.getClass().getName());
        if (interfaze.isInstance(object)) {
            return (T)object;
        }
        throw new Exception("HULU.");
    }

    private static Object invokeConstructor(Constructor<?> ctor) throws Exception {
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = staticServices.get(paramTypes[i]);
        }
        return ctor.newInstance(params);
    }

    private static Constructor<?> getFirstConstructor(Constructor<?>[] constructors) {
        for (Constructor<?> constructor : constructors) {
            boolean foundCtor = true;
            for (Class<?> paramType : constructor.getParameterTypes()) {
                if (!staticServices.containsKey(paramType)) {
                    foundCtor = false;
                    break;
                }
            }
            if (foundCtor) return constructor;
        }
        return null;
    }
}
