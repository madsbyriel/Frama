package services.service_provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sun.net.httpserver.HttpExchange;

import services.cookies.CookieManager;
import services.cookies.ICookieManager;

public class ServiceProvider2 implements IServiceProvider {
    private static Map<Class<?>, Class<?>> staticServiceClasses;
    private static Map<Class<?>, Class<?>> scopedServiceClasses;
    private static Map<Class<?>, Class<?>> transientServiceClasses;
    
    private static Map<Class<?>, Object> staticServiceObjects;
    private Map<Class<?>, Object> scopedServiceObjects;
    private Map<Class<?>, Object> transientServiceObjects;

    private static Map<Long, IServiceProvider> allServices;
    private static Long sessionCount;
    private Long sessionId;

    static {
        staticServiceClasses = new HashMap<>();
        scopedServiceClasses = new HashMap<>();
        transientServiceClasses = new HashMap<>();

        staticServiceObjects = new HashMap<>();

        allServices = new HashMap<>();

        sessionCount = 0L;
    }

    private ServiceProvider2() {
        this.scopedServiceObjects = new HashMap<>();
        this.transientServiceObjects = new HashMap<>();
    }

    public static IServiceProvider getServiceProvider(HttpExchange exchange) {
        IServiceProvider serviceProvider = null;
        ICookieManager cookieManager = new CookieManager(exchange);
        
        Long sessionId = cookieManager.getSessionId();
        if (sessionId == -1) {
            cookieManager.setCookie("sessionId", sessionCount);
            
            serviceProvider = new ServiceProvider2();
            serviceProvider.setSessionId(sessionCount);
            
            allServices.put(sessionCount++, serviceProvider);
            
            return serviceProvider;
        }

        if ((serviceProvider = allServices.get(sessionId)) == null) {
            System.out.println("Couldn't find service from session: " + sessionId);
        }

        return serviceProvider;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> interfaze) {
        Object obj = null;
        Class<?> implementingClass;
        if ((implementingClass = staticServiceClasses.get(interfaze)) != null) {
            if ((obj = staticServiceObjects.get(interfaze)) != null) return (T)obj;
            try {
                return (T)buildStaticObject(implementingClass);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        if ((implementingClass = scopedServiceClasses.get(interfaze)) != null) {
            if ((obj = scopedServiceObjects.get(interfaze)) != null) return (T)obj;
            try {
                return (T)buildScopedObject(implementingClass);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        if ((implementingClass = transientServiceClasses.get(interfaze)) != null) {
            if ((obj = transientServiceObjects.get(interfaze)) != null) return (T)obj;
            try {
                return (T)buildTransientObject(implementingClass);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        System.out.println(interfaze.getName() + " has not been added to Services.");
        return null;
    }

    private static Constructor<?> getFirstConstructor(Constructor<?>[] constructors, Set<Class<?>> availableInterfaces) {
        for (Constructor<?> constructor : constructors) {
            boolean foundConstructor = true;
            for (Class<?> paramType : constructor.getParameterTypes()) {
                if (!availableInterfaces.contains(paramType)) {
                    foundConstructor = false;
                    break;
                }
            }
            if (foundConstructor) return constructor;
        }

        return null;
    }

    private Constructor<?> getConstructorFromServiceInterfaces(Class<?> implementingClass, Map<Class<?>, Class<?>>[] interfaceMaps) {
        Constructor<?>[] constructors = implementingClass.getConstructors();
        Set<Class<?>> availableInterfaces = new HashSet<>();

        for (Map<Class<?>, Class<?>> map : interfaceMaps) {
            availableInterfaces.addAll(map.keySet());
        }

        return getFirstConstructor(constructors, availableInterfaces);
    }

    private List<Object> buildParamsFromParamTypes(Class<?> implementation, Map<Class<?>, Class<?>>[] interfaceMaps, Map<Class<?>, Object>[] objectMaps, Constructor<?> constructor) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        List<Object> parameters = new ArrayList<>();
        for (Class<?> paramType : constructor.getParameterTypes()) {
            Object parameter  = null;

            boolean foundParam = false;
            for (Map<Class<?>, Object> map : objectMaps) {
                if ((parameter = map.get(paramType)) != null) {
                    foundParam = true;
                    break;
                }
            }

            if (!foundParam) {
                Map<Class<?>, Object> mapToAddObjectTo = objectMaps[objectMaps.length - 1];
                Class<?> implementingType = getImplementationFromInterfaceMaps(interfaceMaps, paramType);
                if (implementingType == null) {
                    System.out.println("Didn't find an implementation for interface: " + paramType);
                    return null;
                }
                parameter = buildObjectFromMaps(implementingType, interfaceMaps, objectMaps);
                mapToAddObjectTo.put(paramType, parameter);
            }

            parameters.add(parameter);
        }

        return parameters;
    }

    private Class<?> getImplementationFromInterfaceMaps(Map<Class<?>, Class<?>>[] interfaceMaps, Class<?> interfaze) {
        Class<?> implementation = null;
        for (Map<Class<?>,Class<?>> map : interfaceMaps) {
            if ((implementation = map.get(interfaze)) != null) break;
        }
        return implementation;
    }

    private Object buildObjectFromMaps(Class<?> implementation, Map<Class<?>, Class<?>>[] interfaceMaps, Map<Class<?>, Object>[] objectMaps) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Constructor<?> constructor = getConstructorFromServiceInterfaces(implementation, interfaceMaps);
        
        if (constructor == null) {
            System.out.println("Did not find an invokable constructor for " + implementation.getName() + " from services present.");
            debug();
            return null;
        }

        List<Object> parameters = buildParamsFromParamTypes(implementation, interfaceMaps, objectMaps, constructor);

        Object obj = constructor.newInstance(parameters.toArray(new Object[0]));

        if (obj == null) {
            System.out.println("Couldn't create object from maps.");
            return null;
        }
        if (!implementation.isInstance(obj)) {
            System.out.println("Resulting object (" + obj.getClass().getName() + ") is not an instance of implementing class (" + implementation.getName() + ").");
            return null;
        }

        return obj;
    }

    @SuppressWarnings("unchecked")
    private <T> T buildStaticObject(Class<T> implementation) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (T) buildObjectFromMaps(implementation, new Map[] { staticServiceClasses }, new Map[] { staticServiceObjects });
    }
    
    @SuppressWarnings("unchecked")
    private <T> T buildScopedObject(Class<T> implementation) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (T) buildObjectFromMaps(implementation, new Map[] { staticServiceClasses, scopedServiceClasses }, new Map[] { staticServiceObjects, scopedServiceObjects });
    }
    
    @SuppressWarnings("unchecked")
    private <T> T buildTransientObject(Class<T> implementation) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (T) buildObjectFromMaps(implementation, new Map[] { staticServiceClasses, scopedServiceClasses, transientServiceClasses }, new Map[] { staticServiceObjects, scopedServiceObjects,  transientServiceObjects });
    }

    @Override
    public <T> void addStaticService(Class<T> interfaze, Class<? extends T> clazz) {
        if (interfaze.isAssignableFrom(clazz)) {
            staticServiceClasses.put(interfaze, clazz);
            return;
        }
        System.out.println("Didn't add Service, class does not implement interface.");
    }

    @Override
    public <T> void addScopedService(Class<T> interfaze, Class<? extends T> clazz) {
        if (interfaze.isAssignableFrom(clazz)) {
            scopedServiceClasses.put(interfaze, clazz);
            return;
        }
        System.out.println("Didn't add Service, class does not implement interface.");
    }

    @Override
    public <T> void addTransientService(Class<T> interfaze, Class<? extends T> clazz) {
        if (interfaze.isAssignableFrom(clazz)) {
            transientServiceClasses.put(interfaze, clazz);
            return;
        }
        System.out.println("Didn't add Service, class does not implement interface.");
    }

    @Override
    public <T> void addTransientService(Class<T> interfaze, Class<? extends T> clazz, Object object) {
        if (interfaze.isAssignableFrom(clazz) && clazz.isInstance(object)) {
            transientServiceClasses.put(interfaze, clazz);
            transientServiceObjects.put(interfaze, object);
            return;
        }
        System.out.println("Didn't add Service, class does not implement interface.");
    }

    @Override
    public void clearStaticObjects() {
        staticServiceObjects = new HashMap<>();
    }

    @Override
    public void clearScopedObjects() {
        scopedServiceObjects = new HashMap<>();
    }

    @Override
    public void clearTransientObjects() {
        transientServiceObjects = new HashMap<>();
    }

    @Override
    public <T> T createObjectFromServices(Class<T> classToCreate) {
        T obj;
        
        try {
            obj = buildTransientObject(classToCreate);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }

        if (obj == null) {
            System.out.println("Couldn't creatre object from services.");
            return null;
        }
        if (!classToCreate.isInstance(obj)) {
            System.out.println("Created object is not an implementation of class.");
            return null;
        }
        return obj;
    }

    private void debug() {
        System.out.println("STATIC MAP:");
        for (var entry : staticServiceClasses.entrySet()) {
            System.out.println("\t" + entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("SCOPED MAP:");
        for (var entry : scopedServiceClasses.entrySet()) {
            System.out.println("\t" + entry.getKey() + " : " + entry.getValue());
        }
        System.out.println("TRANSIENT MAP:");
        for (var entry : transientServiceClasses.entrySet()) {
            System.out.println("\t" + entry.getKey() + " : " + entry.getValue());
        }
    }

    @Override
    public Long getSessionId() {
        return sessionId;
    }

    @Override
    public void setSessionId(Long id) {
        sessionId = id;
    }
}
