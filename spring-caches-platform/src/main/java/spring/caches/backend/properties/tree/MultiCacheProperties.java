package spring.caches.backend.properties.tree;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * tbd.
 */
public final class MultiCacheProperties {

    private final Tree properties;

    private MultiCacheProperties(Tree properties) {
        this.properties = properties;
    }

    // For testing purposes only
    public MultiCacheProperties() {
        this(Tree.empty());
    }

    private static String removeIndex(String key) {
        int index = key.indexOf('[');
        return key.substring(0, index == -1 ? key.length() : index);
    }

    public static MultiCacheProperties load(
            Map<String, Object> data,
            Predicate<? super Map.Entry<String, Object>> filter,
            int offset
    ) {
        Tree properties = Tree.empty();
        data.entrySet().stream()
                .filter(filter)
                .forEach(e -> properties.insert(e.getKey().substring(offset), e.getValue()));

        return new MultiCacheProperties(properties);
    }

    public boolean isEmpty() {
        return properties.isEmpty();
    }

    public MultiCacheProperties filterByFactoryName(String factoryName) {
        InnerNode root = new InnerNode(4);
        root.setKey("caches");
        properties.apply((node, inner) -> {
            if (node.getKey().startsWith(factoryName)) {
                inner.addChild(node);
            }
        }, root);
        return new MultiCacheProperties(new Tree(root));
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
}
