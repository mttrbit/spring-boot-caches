package spring.caches.backend.simple;

import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static spring.caches.backend.simple.Simple.UNSET_INT;
import static spring.caches.backend.simple.Simple.requireArgument;

final class SimpleSpec {
    private static final String SPLIT_OPTIONS = ",";
    private static final String SPLIT_KEY_VALUE = "=";

    private final String specification;

    private int initialCapacity = UNSET_INT;
    private boolean recordStats;

    private SimpleSpec(String specification) {
        this.specification = requireNonNull(specification);
    }

    /**
     * Creates a SimpleSpec from a string.
     *
     * @param specification the string form
     * @return the parsed specification
     */
    @SuppressWarnings("StringSplitter")
    public static SimpleSpec parse(String specification) {
        SimpleSpec spec = new SimpleSpec(specification);
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

    Simple toBuilder() {
        Simple builder = Simple.newBuilder();
        if (initialCapacity != UNSET_INT) {
            builder.initialCapacity(initialCapacity);
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
            case "initialCapacity":
                initialCapacity(key, value);
                return;
            case "recordStats":
                recordStats(value);
                return;
            default:
                throw new IllegalArgumentException("Unknown key " + key);
        }
    }

    /**
     * Configures the initial capacity.
     */
    void initialCapacity(String key, @Nullable String value) {
        requireArgument(initialCapacity == UNSET_INT,
                "initial capacity was already set to %,d", initialCapacity);
        initialCapacity = parseInt(key, value);
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
