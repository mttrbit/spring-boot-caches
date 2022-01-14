package spring.caches.backend.properties.tree;

/**
 * tbd.
 * @param <C> tbd.
 */
public interface NodeHandler<C> {
    void handle(Node node, C context);
}
