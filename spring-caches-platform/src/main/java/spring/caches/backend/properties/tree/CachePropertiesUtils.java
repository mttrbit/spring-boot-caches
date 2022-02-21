package spring.caches.backend.properties.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides utility functionality for interacting with application
 * properties.
 */
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.SuspiciousEqualsMethodName"})
public final class CachePropertiesUtils {

    private CachePropertiesUtils() {
    }

    public static Function<CachesProperties.Data, CachesProperties.Data> slice(Function<Node, Boolean> condition) {
        return tree -> {
            List<CachesProperties.Data> data = split(tree, condition);
            return data.isEmpty() ? CachesProperties.Data.empty() : data.get(0);
        };
    }

    /**
     * Divides the properties into n parts ( n >= 0 ) based on the given condition.
     */
    public static Function<CachesProperties.Data, List<CachesProperties.Data>> split(
            Function<Node, Boolean> condition
    ) {
        return tree -> split(tree, condition);
    }

    public static Optional<CachesProperties.Data> findBy(CachesProperties.Data data, Function<Node, Boolean> condition) {
        return data.apply(collectIf(condition), new ArrayList<>()).stream().findFirst().map(CachesProperties.Data::of);
    }

    public static List<CachesProperties.Data> split(CachesProperties.Data data, Function<Node, Boolean> condition) {
        return data.apply(collectIf(condition), new ArrayList<>())
                .stream()
                .map(CachesProperties.Data::of)
                .collect(Collectors.toList());
    }

    public static Function<Node, Boolean> hasKey(String key) {
        return node -> node.getKey().equals(key);
    }

    public static Function<Node, Boolean> keyStartsWith(String key) {
        return node -> node.getKey().startsWith(key);
    }

    public static Function<Node, Boolean> equals(Node other) {
        return node -> node.equals(other);
    }

    public static Function<Node, Boolean> hasChild(Node other) {
        return node -> node instanceof InnerNode && ((InnerNode) node).hasChild(other);
    }

    public static Function<Node, Boolean> hasChild(String key) {
        return node -> node instanceof InnerNode && ((InnerNode) node).hasChild(key);
    }

    public static Function<Node, Boolean> hasLeaf(String key, Object value) {
        return hasChild(new LeafNode(key, value));
    }

    public static Function<Node, Boolean> leafStartsWith(String key, Object value) {
        return hasChild(new LeafNode(key, value));
    }

    public static NodeHandler<List<Node>> collectIf(Function<Node, Boolean> condition) {
        return (node, context) -> {
            if (condition.apply(node)) {
                context.add(node);
            }
        };
    }

    public static Function<Node, List<CachesProperties.Data>> toList(String key) {
        return n -> CachePropertiesUtils
                .split(node -> node.getKey().startsWith(key))
                .apply(CachesProperties.Data.of(n));
    }

    public static NodeHandler<List<Node>> collect() {
        return (node, context) -> context.add(node);
    }

    public static Function<CachesProperties.Data, Object> valueOf(String path) {
        return tree -> tree.find(path)
                .filter(n -> n instanceof LeafNode)
                .map(Node::getValue)
                .orElse(null);
    }

    public static Function<Node, Boolean> isLeaf() {
        return node -> node instanceof LeafNode;
    }

    public static Function<Node, Boolean> isArrayNode() {
        return node -> node.getKey().matches("[^\\s]+\\[\\d*\\]");
    }
}
