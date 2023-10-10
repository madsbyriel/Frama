package services.service_provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import services.cookies.CookieManager;
import services.cookies.ICookieManager;

public class ServiceProvider3 implements IServiceProvider {
    private static Map<Class<?>, Constructor<?>> staticServiceClasses;
    private static Map<Class<?>, Constructor<?>> scopedServiceClasses;
    private static Map<Class<?>, Constructor<?>> transientServiceClasses;

    private static Map<Long, IServiceProvider> serviceProviders;

    private static Map<Class<?>, Object> staticServiceObjects;
    private Map<Class<?>, Object> scopedServiceObjects;
    private Map<Class<?>, Object> transientServiceObjects;

    private static Long sessionCount;

    static {
        staticServiceClasses = new HashMap<>();
        scopedServiceClasses = new HashMap<>();
        transientServiceClasses = new HashMap<>();

        serviceProviders = new HashMap<>();

        staticServiceObjects = new HashMap<>();

        sessionCount = 0l;
    }

    private ServiceProvider3() {
        scopedServiceObjects = new HashMap<>();
        transientServiceObjects = new HashMap<>();
    }

    public static IServiceProvider getEmptyService() {
        return new ServiceProvider3();
    }

    public static IServiceProvider getServiceProvider(HttpExchange exchange) {
        ICookieManager cookieManager = new CookieManager(exchange);
        Long sessionId = cookieManager.getSessionId();

        // If there exists an a service in the map with hash return it.
        IServiceProvider serviceProvider = null;
        if ((serviceProvider = serviceProviders.get(sessionId)) != null) {
            return serviceProvider;
        }

        // Otherwise we'd need to assign a new service to the current session.
        serviceProvider = new ServiceProvider3();
        cookieManager.setCookie("sessionId", sessionCount);
        serviceProviders.put(sessionCount, serviceProvider);
        sessionCount++;

        return serviceProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> interfaze) {
        Object object = null;
        if (staticServiceClasses.containsKey(interfaze)) {
            if ((object = staticServiceObjects.get(interfaze)) != null) {
                return (T)object;
            }
            object = createStaticService(interfaze);
            return (T)object;
        }
        
        if (scopedServiceClasses.containsKey(interfaze)) {
            if ((object = scopedServiceObjects.get(interfaze)) != null) {
                return (T)object;
            }
            object = createScopedService(interfaze);
            return (T)object;
        }

        if (transientServiceClasses.containsKey(interfaze)) {
            if ((object = transientServiceObjects.get(interfaze)) != null) {
                return (T)object;
            }
            object = createTransientService(interfaze);
            return (T)object;
        }
        return null;
    }

    // Adding services. Make sure each added service can actually be created from present services.
    @Override
    public <T> void addStaticService(Class<T> interfaze, Class<? extends T> clazz) {
        if (!interfaze.isAssignableFrom(clazz)) {
            System.out.println("Couldn't add " + interfaze.getName() + ". Class (" + clazz.getName() + ") parameter does not extend or implement interface.");
            return;
        }

        Set<Class<?>> paramTypes = new HashSet<>();
        paramTypes.addAll(staticServiceClasses.keySet());
        Constructor<?> constructor = findFirstConstructor(clazz, paramTypes);

        if (constructor == null) {
            System.out.println("Did not find a constructor for " + clazz.getName() + " with interface "  + interfaze.getName() + ".");
            return;
        }

        staticServiceClasses.put(interfaze, constructor);
    }

    @Override
    public <T> void addScopedService(Class<T> interfaze, Class<? extends T> clazz) {
        if (!interfaze.isAssignableFrom(clazz)) {
            System.out.println("Couldn't add " + interfaze.getName() + ". Class (" + clazz.getName() + ") parameter does not extend or implement interface.");
            return;
        }

        Set<Class<?>> paramTypes = new HashSet<>();
        paramTypes.addAll(staticServiceClasses.keySet());
        paramTypes.addAll(scopedServiceClasses.keySet());
        Constructor<?> constructor = findFirstConstructor(clazz, paramTypes);

        if (constructor == null) {
            System.out.println("Did not find a constructor for " + clazz.getName() + " with interface "  + interfaze.getName() + ".");
            return;
        }

        scopedServiceClasses.put(interfaze, constructor);
    }

    @Override
    public <T> void addTransientService(Class<T> interfaze, Class<? extends T> clazz) {
        if (!interfaze.isAssignableFrom(clazz)) {
            System.out.println("Couldn't add " + interfaze.getName() + ". Class (" + clazz.getName() + ") parameter does not extend or implement interface.");
            return;
        }

        Set<Class<?>> paramTypes = new HashSet<>();
        paramTypes.addAll(staticServiceClasses.keySet());
        paramTypes.addAll(scopedServiceClasses.keySet());
        paramTypes.addAll(transientServiceClasses.keySet());
        Constructor<?> constructor = findFirstConstructor(clazz, paramTypes);

        if (constructor == null) {
            System.out.println("Did not find a constructor for " + clazz.getName() + " with interface "  + interfaze.getName() + ".");
            return;
        }

        transientServiceClasses.put(interfaze, constructor);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createObjectFromServices(Class<T> classToCreate) {
        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.addAll(transientServiceClasses.keySet());
        interfaces.addAll(scopedServiceClasses.keySet());
        interfaces.addAll(staticServiceClasses.keySet());

        Constructor<?> constructor = findFirstConstructor(classToCreate, interfaces);

        if (constructor == null) {
            System.out.println("Did not find a constructor for object " + classToCreate.getName());
            return null;
        }

        Class<?>[] paramTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object param = null;
            if (staticServiceClasses.containsKey(paramType)) {
                if ((param = staticServiceObjects.get(paramType)) == null) {
                    param = createStaticService(paramType);
                }
            }
            else if (scopedServiceClasses.containsKey(paramType)) {
                if ((param = scopedServiceObjects.get(paramType)) == null) {
                    param = createScopedService(paramType);
                }
            }
            else if (transientServiceClasses.containsKey(paramType)) {
                if ((param = transientServiceObjects.get(paramType)) == null) {
                    param = createTransientService(paramType);
                }
            }

            parameters[i] = param;
        }

        try {
            return (T)constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            System.out.println("Something went wrong when creating some object from services.");
            return null;
        }
    }

    @Override
    public void clearStaticObjects() {
        staticServiceObjects.clear();
    }

    @Override
    public void clearScopedObjects() {
        scopedServiceObjects.clear();
    }

    @Override
    public void clearTransientObjects() {
        transientServiceObjects.clear();
    }

    // Non overriding methods:
    private Object createTransientService(Class<?> interfaze) {
        if (!transientServiceClasses.containsKey(interfaze)) {
            System.out.println("There is no transient service " + interfaze.getName() + ".");
            return null;
        }

        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.addAll(staticServiceClasses.keySet());
        interfaces.addAll(scopedServiceClasses.keySet());
        interfaces.addAll(transientServiceClasses.keySet());

        // Check if it is even possible to create the service.
        Constructor<?> constructor = transientServiceClasses.get(interfaze);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (!interfaces.contains(paramType)) {
                System.out.println("There is no service " + paramType.getName() + ".");
                return null;
            }
        }
        
        // Create the service by using the constructor in map.
        Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object param = null;
            if (staticServiceClasses.containsKey(paramType)) {
                if ((param = staticServiceObjects.get(paramType)) == null) {
                    param = createStaticService(paramType);
                }
            }
            else if (scopedServiceClasses.containsKey(paramType)) {
                if ((param = scopedServiceObjects.get(paramType)) == null) {
                    param = createScopedService(paramType);
                }
            }
            else if (transientServiceClasses.containsKey(paramType)) {
                if ((param = transientServiceObjects.get(paramType)) == null) {
                    param = createTransientService(paramType);
                }
            }
            
            parameters[i] = param;
        }
        
        try {
            Object object = constructor.newInstance(parameters);
            transientServiceObjects.put(interfaze, object);
            return object;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            System.out.println("Something went wrong creating static service.");
            return null;
        }
    }

    private Object createScopedService(Class<?> interfaze) {
        if (!scopedServiceClasses.containsKey(interfaze)) {
            System.out.println("There is no scoped service " + interfaze.getName() + ".");
            return null;
        }

        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.addAll(staticServiceClasses.keySet());
        interfaces.addAll(scopedServiceClasses.keySet());

        // Check if it is even possible to create the service.
        Constructor<?> constructor = scopedServiceClasses.get(interfaze);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (!interfaces.contains(paramType)) {
                System.out.println("There is no service " + paramType.getName() + ".");
                return null;
            }
        }
        
        // Create the service by using the constructor in map.
        Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object param = null;
            if (staticServiceClasses.containsKey(paramType)) {
                if ((param = staticServiceObjects.get(paramType)) == null) {
                    param = createStaticService(paramType);
                }
            }
            else if (scopedServiceClasses.containsKey(paramType)) {
                if ((param = scopedServiceObjects.get(paramType)) == null) {
                    param = createScopedService(paramType);
                }
            }
            
            parameters[i] = param;
        }
        
        try {
            Object object = constructor.newInstance(parameters);
            scopedServiceObjects.put(interfaze, object);
            return object;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            System.out.println("Something went wrong creating static service.");
            return null;
        }
    }

    private Object createStaticService(Class<?> interfaze) {
        if (!staticServiceClasses.containsKey(interfaze)) {
            System.out.println("There is no static service " + interfaze.getName() + ".");
            return null;
        }

        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.addAll(staticServiceClasses.keySet());
        

        // Check if it is even possible to create the service.
        Constructor<?> constructor = staticServiceClasses.get(interfaze);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (!interfaces.contains(paramType)) {
                System.out.println("There is no service " + paramType.getName() + ".");
                return null;
            }
        }
        
        // Create the service by using the constructor in map.
        Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object param = null;
            if (staticServiceClasses.containsKey(paramType)) {
                if ((param = staticServiceObjects.get(paramType)) == null) {
                    param = createStaticService(paramType);
                }
            }
            
            parameters[i] = param;
        }
        
        try {
            Object object = constructor.newInstance(parameters);
            staticServiceObjects.put(interfaze, object);
            return object;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            System.out.println("Something went wrong creating static service.");
            return null;
        }
    }
    
    private Constructor<?> findFirstConstructor(Class<?> clazz, Set<Class<?>> allParamTypes) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            boolean foundConstructor = true;
            for (Class<?> parameterType : constructor.getParameterTypes()) {
                if (!allParamTypes.contains(parameterType)) {
                    foundConstructor = false; // Assuming each previous interface actually can be made
                    break;
                }
            }
            if (foundConstructor) return constructor;
        }

        return null;
    }

    @Override
    public Long getSessionId() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSessionId'");
    }

    @Override
    public void setSessionId(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSessionId'");
    }
}
