package spring.caches.backend.properties.tree;

import java.util.Map;
import java.util.function.Consumer;

/**
 * tbd.
 */
public final class CachesProperties {

    private final Tree properties;

    private CachesProperties(Tree properties) {
        this.properties = properties;
    }

    // For testing purposes only
    public CachesProperties() {
        this(Tree.empty());
    }

    private static String removeIndex(String key) {
        int index = key.indexOf('[');
        return key.substring(0, index == -1 ? key.length() : index);
    }

    public static CachesProperties resolve(Resolvable resolvable) {
        Map<String, Object> data = resolvable.resolve();
        Tree properties = Tree.empty();
        data.forEach(properties::insert);
        return new CachesProperties(properties);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public CachesProperties filterByFactoryName(String factoryName) {
        InnerNode root = new InnerNode(4);
        root.setKey("caches");
        properties.apply((node, inner) -> {
            if (node.getKey().startsWith(factoryName)) {
                inner.addChild(node);
            }
        }, root);
        return new CachesProperties(new Tree(root));
    }

    public void consume(Consumer<? super Tree> consumer) {
        InnerNode root = (InnerNode) properties.getRoot(); // caches
        if (root == null) {
            return;
        }
        for (Node node : root.getChildren()) { // Backend[*]
            if (node != null) {
                InnerNode child = new InnerNode((InnerNode) node);
                child.setKey(removeIndex(node.getKey()));
                consumer.accept(new Tree(child));
            }
        }
    }

    /**
     * tbd.
     */
    public interface Resolvable {
        Map<String, Object> resolve();
    }
}
