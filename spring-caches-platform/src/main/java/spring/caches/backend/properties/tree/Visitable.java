package spring.caches.backend.properties.tree;

/**
 * tbd.
 * @param <T> tbd.
 */
@FunctionalInterface
public interface Visitable<T> {
    void accept(T visitor);
}
