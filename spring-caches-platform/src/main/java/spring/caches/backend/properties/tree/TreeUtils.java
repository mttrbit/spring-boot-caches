package spring.caches.backend.properties.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides utility functionality for interacting with application
 * properties.
 */
@SuppressWarnings({"PMD.LinguisticNaming", "PMD.SuspiciousEqualsMethodName"})
public final class TreeUtils {

    private TreeUtils() {
    }

    public static Function<Tree, Tree> subtree(Function<Node, Boolean> condition) {
        return tree ->
                tree.apply(collectIf(condition), new ArrayList<>()).stream()
                        .map(Tree::of)
                        .findFirst()
                        .orElse(Tree.empty());
    }

    public static Function<Tree, List<Tree>> subtrees(Function<Node, Boolean> condition) {
        return tree ->
                tree.apply(collectIf(condition), new ArrayList<>()).stream()
                        .map(Tree::of)
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

    public static NodeHandler<List<Node>> collect() {
        return (node, context) -> context.add(node);
    }

    public static Function<Tree, Object> valueOf(String path) {
        return tree -> tree.find(path)
                .filter(n -> n instanceof LeafNode)
                .map(n -> ((LeafNode) n).getValue())
                .orElse(null);
    }

    public static Function<Node, Boolean> isLeaf() {
        return node -> node instanceof LeafNode;
    }

    public static Function<Node, Boolean> isArrayNode() {
        return node -> node.getKey().matches("[^\\s]+\\[\\d*\\]");
    }
}
