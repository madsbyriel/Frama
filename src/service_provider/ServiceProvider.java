package service_provider;
import com.sun.net.httpserver.HttpExchange;

import services.cookies.CookieManager;
import services.cookies.ICookieManager;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    private static long sessionCount;
    private static Map<Class<?>, Object> staticServices;
    private static Map<Long, ServiceProvider> scopedServiceProvider;

    private Map<Class<?>, Object> scopedServices;
    private long sessionId;

    static {
        staticServices = new HashMap<>();
        scopedServiceProvider = new HashMap<>();
        staticServices.put(ICookieManager.class, new CookieManager((HttpExchange)staticServices.get(HttpExchange.class)));
        sessionCount = 0;
    }


    private ServiceProvider(HttpExchange exchange, long sessionId) {
        this.scopedServices = new HashMap<>();
        this.scopedServices.put(HttpExchange.class, exchange);
        this.scopedServices.put(ServiceProvider.class, this);
        this.sessionId = sessionId;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public static ServiceProvider getScopedServiceProvider(HttpExchange exchange) {
        ICookieManager cookieManager;

        try {
            cookieManager = getStaticService(ICookieManager.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        long sessionId = cookieManager.getSessionId();
        ServiceProvider serviceProvider;

        if (sessionId != -1 && (serviceProvider = scopedServiceProvider.get(sessionId)) != null) {
            serviceProvider.setExchange(exchange);
            return serviceProvider;
        }

        serviceProvider = new ServiceProvider(exchange, sessionCount);
        scopedServiceProvider.put(sessionCount, serviceProvider);

        cookieManager.setCookie("sessionId", sessionCount);

        ++sessionCount;

        return serviceProvider;
    }

    public static void addStaticService(Class<?> interfaze, Class<?> implementation) throws Exception  {
        Constructor<?>[] ctors = implementation.getConstructors();
        Constructor<?> ctor = getFirstConstructor(ctors);
        if (ctor == null) throw new IllegalArgumentException("Didn't find a constructor.");
        staticServices.put(interfaze, invokeConstructor(ctor));
    }

    @SuppressWarnings("unchecked")
    public static <T> T getStaticService(Class<T> interfaze) throws Exception {
        Object object = staticServices.get(interfaze);
        if (object == null) throw new Exception("Couldn't find the service you are looking for. Perhaps it is not yet created, or added to the service provider");
        if (interfaze.isInstance(object)) {
            return (T)object;
        }
        throw new Exception("HULU.");
    }

    public static Object invokeConstructor(Constructor<?> ctor) throws Exception {
        Class<?>[] paramTypes = ctor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];
        for (int i = 0; i < params.length; i++) {
            params[i] = staticServices.get(paramTypes[i]);
        }
        return ctor.newInstance(params);
    }

    public static Constructor<?> getFirstConstructor(Constructor<?>[] constructors) {
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

    public Constructor<?> getConstructor(Constructor<?>[] constructors) {
        for (Constructor<?> constructor : constructors) {
            boolean foundCtor = true;
            for (Class<?> paramType : constructor.getParameterTypes()) {
                if (!staticServices.containsKey(paramType) && !scopedServices.containsKey(paramType)) {
                    foundCtor = false;
                    break;
                }
            }
            if (foundCtor) return constructor;
        }
        return null;
    }

    public Object invokeFirstConstructor(Constructor<?>[] constructors) throws Exception {
        Constructor<?> constructor = getConstructor(constructors);
        
        if (constructor == null) throw new Exception("Didn't find constructor.");
        
        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] params = new Object[paramTypes.length];

        for (int i = 0; i < params.length; i++) {
            Object obj;
            if ((obj = staticServices.get(paramTypes[i])) != null || (obj = scopedServices.get(paramTypes[i])) != null) {
                params[i] = obj;
            }
            else {
                throw new Exception("Did not find parameters in services.");
            }
        }

        return constructor.newInstance(params);
    }

    public void setExchange(HttpExchange exchange) {
        this.scopedServices.put(HttpExchange.class, exchange);
    }
}
