package service_provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServiceProvider3 implements IServiceProvider {
    private static Map<Class<?>, Constructor<?>> staticServiceClasses;
    private static Map<Class<?>, Constructor<?>> scopedServiceClasses;
    private static Map<Class<?>, Constructor<?>> transientServiceClasses;

    private static Map<Long, IServiceProvider> serviceProviders;

    private static Map<Class<?>, Object> staticServiceObjects;
    private Map<Class<?>, Object> scopedServiceObjects;
    private Map<Class<?>, Object> transientServiceObjects;

    static {
        staticServiceClasses = new HashMap<>();
        scopedServiceClasses = new HashMap<>();
        transientServiceClasses = new HashMap<>();

        serviceProviders = new HashMap<>();

        staticServiceObjects = new HashMap<>();
    }

    private ServiceProvider3() {
        scopedServiceObjects = new HashMap<>();
        transientServiceObjects = new HashMap<>();
    }

    public static IServiceProvider getServiceProvider(Long sessionHash) {
        // If there exists an a service in the map with hash return it.
        IServiceProvider serviceProvider = null;
        if ((serviceProvider = serviceProviders.get(sessionHash)) != null) {
            return serviceProvider;
        }

        // Otherwise we'd need to assign a new service to the current session.
        serviceProvider = new ServiceProvider3();

        return serviceProvider;
    }

    @Override
    public <T> T getService(Class<T> interfaze) {
        Object object = null;
        if (staticServiceClasses.containsKey(interfaze)) {
            if ((object = staticServiceObjects.get(interfaze)) != null) {
                return (T)object;
            }
            object = createStaticService(interfaze);
            staticServiceObjects.put(interfaze, object);
            return (T)object;
        }
        
        if (scopedServiceClasses.containsKey(interfaze)) {
            if ((object = scopedServiceObjects.get(interfaze)) != null) {
                return (T)object;
            }
            object = createStaticService(interfaze);
            scopedServiceObjects.put(interfaze, object);
            return (T)object;
        }

        if (transientServiceClasses.containsKey(interfaze)) {
            if ((object = transientServiceObjects.get(interfaze)) != null) {
                return (T)object;
            }
            object = createStaticService(interfaze);
            transientServiceObjects.put(interfaze, object);
            return (T)object;
        }
        return null;
    }

    private Object createStaticService(Class<?> interfaze) {
        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.addAll(staticServiceClasses.keySet());
        
        if (!interfaces.contains(interfaze)) {
            System.out.println("There is no static service " + interfaze.getName() + ".");
            return null;
        }

        // Check if it is even possible to create the service.
        Constructor<?> constructor = staticServiceClasses.get(interfaze);
        Class<?>[] paramTypes = constructor.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            if (!interfaces.contains(paramType)) {
                System.out.println("There is no static service " + paramType.getName() + ".");
                return null;
            }
        }
        
        // Create the service by using the constructor in map.
        Object[] parameters = new Object[paramTypes.length];
        for (int i = 0; i < parameters.length; i++) {
            Class<?> paramType = paramTypes[i];
            Object param = null;
            if (!staticServiceObjects.containsKey(paramType)) {
                param = staticServiceObjects.get(paramType);
            }
            else {
                param = createStaticService(paramType);
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
    public <T> T createObjectFromServices(Class<T> classToCreate) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createObjectFromServices'");
    }

    @Override
    public void clearStaticObjects() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearStaticObjects'");
    }

    @Override
    public void clearScopedObjects() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearScopedObjects'");
    }

    @Override
    public void clearTransientObjects() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clearTransientObjects'");
    }

    // Non overriding methods:
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
}
