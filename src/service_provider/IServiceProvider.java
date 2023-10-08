package service_provider;

public interface IServiceProvider {
    <T> T getService(Class<T> interfaze);
    <T> void addStaticService(Class<T> interfaze, Class<? extends T> clazz);
    <T> void addScopedService(Class<T> interfaze, Class<? extends T> clazz);
    <T> void addTransientService(Class<T> interfaze, Class<? extends T> clazz);
    <T> void addTransientService(Class<T> interfaze, Class<? extends T> clazz, Object object);

    <T> T createObjectFromServices(Class<T> classToCreate);

    void clearStaticObjects();
    void clearScopedObjects();
    void clearTransientObjects();
}
