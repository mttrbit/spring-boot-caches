package spring.caches.backend;

import spring.caches.backend.system.BackendFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * The {@code Platform} class is responsible for providing any platform specific APIs,
 * including the mechanism by which cache backends are created.
 */
public abstract class Platform {

    @SuppressWarnings("ConstantField")
    private static final String DEFAULT_PLATFORM =
            "spring.caches.backend.system.DefaultPlatform";

    // The first available platform from this list is used. Each platform is defined separately
    // outside this array so that the IdentifierNameString annotation can be applied to each.
    private static final String[] AVAILABLE_PLATFORMS =
            {
                    // The fallback/default platform gives a workable, cache backend.
                    DEFAULT_PLATFORM
            };

    /**
     * Returns a cache backend of the given class name for use by a Spring Boot Caches Provider. Note that the
     * returned backend need not be unique; one backend could be used by multiple cache providers. The given
     * class name must be in the normal dot-separated form (e.g. "com.example.Foo$Bar") rather than
     * the internal binary format (e.g. "com/example/Foo$Bar").
     *
     * @param type the fully-qualified name of the Java class to which the cache is associated.
     *             The cache name is derived from this string in a platform specific way.
     */
    public static BackendFactory getBackend(String type) {
        return LazyHolder.INSTANCE.getBackendFactory(type);
    }

    public static Set<String> getBackendFactoryNames() {
        return LazyHolder.INSTANCE.registeredFactories();
    }

    /**
     * Returns a human-readable string describing the platform and its configuration. This should
     * contain everything a human would need to see to check that the Platform was configured as
     * expected. It should contain the platform name along with any configurable elements
     * (e.g. cache providers) and their settings. It is recommended (though not required) that this
     * string is formatted with one piece of configuration per line in a tabular format, such as:
     * <pre>{@code
     * platform: <human readable name>
     * }</pre>
     * It is not required that this string be machine parseable (though it should be stable).
     */
    public static String getConfigInfo() {
        return LazyHolder.INSTANCE.getConfigInfoImpl();
    }

    protected abstract BackendFactory getBackendFactory(String type);

    protected abstract Set<String> registeredFactories();

    protected abstract String getConfigInfoImpl();

    // Use the lazy holder idiom here to avoid class loading issues. Loading the Platform sub-class
    // will trigger static initialization of the Platform class first, which would not be possible if
    // the INSTANCE field were a static field in Platform. This means that any errors in platform
    // loading are deferred until the first time one of the Platform's static methods is invoked.
    private static final class LazyHolder {
        private static final Platform INSTANCE = loadFirstAvailablePlatform(AVAILABLE_PLATFORMS);

        private static Platform loadFirstAvailablePlatform(String... platformClasses) {
            StringBuilder errorMessage = new StringBuilder();
            // Try the reflection-based approach as a backup, if the provider isn't available.
            for (String clazz : platformClasses) {
                try {
                    return (Platform) Class.forName(clazz).getConstructor().newInstance();
                } catch (Throwable e) {
                    // Catch errors so if we can't find _any_ implementations, we can report something useful.
                    // Unwrap any generic wrapper exceptions for readability here (extend this as needed).
                    if (e instanceof InvocationTargetException) {
                        e = e.getCause();
                    }
                    errorMessage.append('\n').append(clazz).append(": ").append(e);
                }
            }
            throw new IllegalStateException(
                    errorMessage.insert(0, "No caching platforms found:").toString());
        }
    }
}
