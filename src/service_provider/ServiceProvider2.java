package service_provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceProvider2 implements IServiceProvider {
    private static Map<Class<?>, Class<?>> staticServiceClasses;
    private static Map<Class<?>, Class<?>> scopedServiceClasses;
    private static Map<Class<?>, Class<?>> transientServiceClasses;
    
    private static Map<Class<?>, Object> staticServiceObjects;
    private Map<Class<?>, Object> scopedServiceObjects;
    private Map<Class<?>, Object> transientServiceObjects;

    static {
        staticServiceClasses = new HashMap<>();
        scopedServiceClasses = new HashMap<>();
        transientServiceClasses = new HashMap<>();

        staticServiceObjects = new HashMap<>();
    }

    @Override
    public <T> T getService(Class<T> interfaze) {
        Object obj = null;
        Class<?> implementingClass;
        if ((implementingClass = staticServiceClasses.get(interfaze)) != null) {
            if ((obj = staticServiceObjects.get(interfaze)) != null) return (T)obj;
            return (T)buildStaticObject(implementingClass);
        }
        
        if ((implementingClass = scopedServiceClasses.get(interfaze)) != null) {
            if ((obj = scopedServiceObjects.get(interfaze)) != null) return (T)obj;
            return (T)buildScopedObject(implementingClass);
        }
        
        if ((implementingClass = transientServiceClasses.get(interfaze)) != null) {
            if ((obj = transientServiceObjects.get(obj)) != null) return (T)obj;
            return (T)buildTransientObject(implementingClass);

        }

        System.out.println(interfaze.getName() + " has not been added to Services.");
        return null;
    }

    private static Constructor<?> getFirstConstructor(Constructor<?>[] constructors) {

        return null;
    }
    
    private <T> T buildStaticObject(Class<T> implementation) {
        Constructor<?>[] constructors = implementation.getConstructors();
        Constructor<?> constructor = getFirstConstructor(constructors);
        List<Object> parameters = new ArrayList<>();
        for (Parameter paramType : constructor.getParameters()) {
            Object parameter;
            if ((parameter = staticServiceObjects.get(paramType.getType())) == null) {
                parameter = buildStaticObject(paramType.getType());
                staticServiceObjects.put(paramType.getType(), parameter);
            }
            parameters.add(parameter);
        }
        
        return null;
    }
    
    private <T> T buildScopedObject(Class<T> implementation) {
        Constructor<?>[] constructors = implementation.getConstructors();
        Constructor<?> constructor = getFirstConstructor(constructors);
        List<Object> parameters = new ArrayList<>();
        for (Parameter paramType : constructor.getParameters()) {
            Object parameter;
            if ((parameter = staticServiceObjects.get(paramType.getType())) == null) {
                parameter = buildStaticObject(paramType.getType());
            }
            parameters.add(parameter);
        }
        
        return null;
    }    
    
    private <T> T buildTransientObject(Class<T> implementation) {
        return null;
    }

    @Override
    public <T> void addStaticService(Class<T> interfaze, Class<? extends T> clazz) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addStaticService'");
    }

    @Override
    public <T> void addScopedService(Class<T> interfaze, Class<? extends T> clazz) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addScopedService'");
    }

    @Override
    public <T> void addTransientService(Class<T> interfaze, Class<? extends T> clazz) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addTransientService'");
    }
}
