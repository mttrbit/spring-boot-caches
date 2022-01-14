package spring.caches.backend.properties.tree;

/**
 * tbd.
 * @param <T> tbd.
 */
@FunctionalInterface
public interface Visitor<T> {
    void visit(T node);
}
