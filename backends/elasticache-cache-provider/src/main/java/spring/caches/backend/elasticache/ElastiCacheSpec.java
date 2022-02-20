package spring.caches.backend.elasticache;

import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static spring.caches.backend.elasticache.ElastiCache.UNSET_INT;
import static spring.caches.backend.elasticache.ElastiCache.requireArgument;

final class ElastiCacheSpec {
    private static final String SPLIT_OPTIONS = ",";
    private static final String SPLIT_KEY_VALUE = "=";

    private final String specification;

    private boolean recordStats;
    private int expiration = UNSET_INT;

    private ElastiCacheSpec(String specification) {
        this.specification = requireNonNull(specification);
    }

    /**
     * Creates a SimpleSpec from a string.
     *
     * @param specification the string form
     * @return the parsed specification
     */
    @SuppressWarnings("StringSplitter")
    public static ElastiCacheSpec parse(String specification) {
        ElastiCacheSpec spec = new ElastiCacheSpec(specification);
        for (String option : specification.split(SPLIT_OPTIONS)) {
            spec.parseOption(option.trim());
        }
        return spec;
    }

    static int parseInt(String key, @Nullable String value) {
        requireArgument((value != null) && !value.isEmpty(), "value of key %s was omitted", key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format(
                    "key %s value was set to %s, must be an integer", key, value), e);
        }
    }

    ElastiCache toBuilder() {
        ElastiCache builder = ElastiCache.newBuilder();

        if (expiration != UNSET_INT) {
            builder.expiration(expiration);
        }
        if (recordStats) {
            builder.recordStats();
        }
        return builder;
    }

    /**
     * Parses and applies the configuration option.
     */
    void parseOption(String option) {
        if (option.isEmpty()) {
            return;
        }

        @SuppressWarnings("StringSplitter")
        String[] keyAndValue = option.split(SPLIT_KEY_VALUE);
        requireArgument(keyAndValue.length <= 2,
                "key-value pair %s with more than one equals sign", option);

        String key = keyAndValue[0].trim();
        String value = (keyAndValue.length == 1) ? null : keyAndValue[1].trim();

        configure(key, value);
    }

    /**
     * Configures the setting.
     */
    void configure(String key, @Nullable String value) {
        switch (key) {
            case "expiration":
                expiration(key, value);
                return;
            case "recordStats":
                recordStats(value);
                return;
            default:
                throw new IllegalArgumentException("Unknown key " + key);
        }
    }

    /**
     * Configures the expiration.
     */
    void expiration(String key, @Nullable String value) {
        requireArgument(expiration == UNSET_INT,
                "initial capacity was already set to %,d", expiration);
        expiration = parseInt(key, value);
    }

    /**
     * Configures the value as weak or soft references.
     */
    void recordStats(@Nullable String value) {
        requireArgument(value == null, "record stats does not take a value");
        requireArgument(!recordStats, "record stats was already set");
        recordStats = true;
    }

    /**
     * Returns a string that can be used to parse an equivalent {@code SimpleSpec}. The order and
     * form of this representation is not guaranteed, except that parsing its output will produce a
     * {@code SimpleSpec} equal to this instance.
     *
     * @return a string representation of this specification
     */
    public String toSpecificationString() {
        return specification;
    }

    /**
     * Returns a string representation for this {@code SimpleSpec} instance. The form of this
     * representation is not guaranteed.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + '{' + toSpecificationString() + '}';
    }
}
