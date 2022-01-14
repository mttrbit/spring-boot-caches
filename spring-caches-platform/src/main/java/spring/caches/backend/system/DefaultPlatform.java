package spring.caches.backend.system;

import spring.caches.backend.Platform;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * This is the default cache platform.
 */
public class DefaultPlatform extends Platform {

    // comma separated list of fully qualified class names
    private static final String BACKEND_FACTORY = "cache.backend_factories";

    private final Map<String, BackendFactory> backendFactories;

    public DefaultPlatform() {
        Map<String, BackendFactory> backendFactories = loadServices(BackendFactory.class, BACKEND_FACTORY);
        this.backendFactories = backendFactories != null ? backendFactories : newSimpleBackend();
    }

    // Visible for testing
    DefaultPlatform(Map<String, BackendFactory> backendFactories) {
        this.backendFactories = backendFactories;
    }

    /**
     * Attempts to load an implementation of the given {@code serviceType}.
     */
    private static Map<String, BackendFactory> loadServices(Class<BackendFactory> serviceType, String systemProperty) {
        Map<String, BackendFactory> service = getInstancesFromSystemProperty(systemProperty, serviceType);
        if (service != null) {
            // Factories were loaded successfully via an explicitly overridden system property.
            return service;
        }

        Map<String, BackendFactory> loadedServices = new HashMap<>();
        for (BackendFactory loaded : ServiceLoader.load(serviceType)) {
            if (loadedServices.containsKey(loaded.toString())) {
                System.err.printf(
                        "Multiple implementations of cache backend %s found on the classpath: %s%n"
                                + "Ensure only the service implementation you want to use is included on the "
                                + "classpath or else specify the service class at startup with the '%s' system "
                                + "property. The default implementation will be used instead.%n",
                        serviceType.getName(), loadedServices, systemProperty);
            }
            loadedServices.put(loaded.toString(), loaded);
        }

        return loadedServices.isEmpty() ? null : loadedServices;
    }

    public static Map<String, BackendFactory> getInstancesFromSystemProperty(
            String propertyName,
            Class<BackendFactory> type
    ) {
        return getInstancesFromSystemProperty(propertyName, null, type);
    }

    @SuppressWarnings({"ReturnCount", "CyclomaticComplexity", "MultipleStringLiterals"})
    public static Map<String, BackendFactory> getInstancesFromSystemProperty(
            String propertyName,
            String defaultValue,
            Class<BackendFactory> type
    ) {
        String property = readProperty(propertyName, defaultValue);
        if (property == null) {
            return null;
        }

        Map<String, BackendFactory> backendFactoryMap = new LinkedHashMap<>(16);
        String getInstanceLiteral = "getInstance";
        for (String factory : property.split(",")) {
            String normalizedFactory = factory.strip();
            int hashIndex = normalizedFactory.indexOf('#');
            String className = hashIndex == -1 ? normalizedFactory : normalizedFactory.substring(0, hashIndex);
            String methodName = hashIndex == -1 ? getInstanceLiteral : normalizedFactory.substring(hashIndex + 1);

            String attemptedMethod = className + '#' + methodName + "()";
            try {
                Class<?> clazz = Class.forName(className);
                try {
                    Method method = clazz.getMethod(methodName);
                    // If the method exists, try to invoke it and don't fall back to the constructor if it
                    // fails. The fallback is only for the case where the method in question has been removed.
                    BackendFactory backendFactory = type.cast(method.invoke(null));
                    backendFactoryMap.put(backendFactory.toString(), backendFactory);
                    continue;
                } catch (NoSuchMethodException e) {
                    // If the user explicitly specified a getInstance method via "ClassName#getInstance" and
                    // that getInstance method doesn't exist, fall back to constructor invocation. This allows
                    // system properties that were set for cache backend types Spring Caches provides to continue
                    // to work even though we intentionally removed their getInstance() methods.
                    if (hashIndex == -1 || !getInstanceLiteral.equals(methodName)) {
                        // Otherwise, error and return
                        error("method '%s' does not exist: %s\n", normalizedFactory, e);
                        return null;
                    }
                }

                // The method didn't exist, try the constructor
                attemptedMethod = "new " + className + "()";
                BackendFactory backendFactory = type.cast(clazz.getConstructor().newInstance());
                backendFactoryMap.put(backendFactory.toString(), backendFactory);
            } catch (ClassNotFoundException e) {
                // Expected if an optional aspect is not being used (no error).
            } catch (ClassCastException e) {
                error("cannot cast result of calling '%s' to '%s': %s\n", attemptedMethod, type.getName(), e);
            } catch (Exception e) {
                error(
                        "cannot call expected no-argument constructor or static method '%s': %s\n",
                        attemptedMethod, e);
            }
        }
        return backendFactoryMap.isEmpty() ? null : backendFactoryMap;
    }

    private static String readProperty(String propertyName, String defaultValue) {
        Objects.requireNonNull(propertyName, "property name");
        try {
            return System.getProperty(propertyName, defaultValue);
        } catch (SecurityException e) {
            error("cannot read property name %s: %s", propertyName, e);
        }
        return null;
    }

    private static void error(String msg, Object... args) {
        System.err.println(DefaultPlatform.class + ": " + String.format(msg, args));
    }

    private Map<String, BackendFactory> newSimpleBackend() {
        BackendFactory simple = DefaultBackendFactory.getInstance();
        return Collections.singletonMap(simple.toString(), simple);
    }

    @Override
    protected BackendFactory getBackendFactory(String type) {
        return backendFactories.get(type);
    }

    @Override
    protected Set<String> registeredFactories() {
        return backendFactories.keySet();
    }

    @Override
    protected String getConfigInfoImpl() {
        return "Platform: " + getClass().getName() + "\n"
                + "BackendFactories: " + backendFactories.values();
    }
}
