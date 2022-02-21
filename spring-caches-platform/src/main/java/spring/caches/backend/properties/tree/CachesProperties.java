package spring.caches.backend.properties.tree;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * tbd.
 */
public final class CachesProperties {

    private final Data data;

    private CachesProperties(Data data) {
        this.data = data;
    }

    // For testing purposes only
    public CachesProperties() {
        this(Data.empty());
    }

    private static String removeIndex(String key) {
        int index = key.indexOf('[');
        return key.substring(0, index == -1 ? key.length() : index);
    }

    public static CachesProperties resolve(Resolvable resolvable) {
        Map<String, Object> data = resolvable.resolve();
        Data properties = Data.empty();
        data.forEach(properties::insert);
        return new CachesProperties(properties);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public CachesProperties filterByFactoryName(String factoryName) {
        InnerNode root = new InnerNode(4);
        root.setKey("caches");
        data.apply((node, inner) -> {
            if (node.getKey().startsWith(factoryName)) {
                inner.addChild(node);
            }
        }, root);
        return new CachesProperties(new Data(root));
    }

    public void consume(Consumer<? super Data> consumer) {
        InnerNode root = (InnerNode) data.getRoot(); // caches
        if (root == null) {
            return;
        }
        for (Node node : root.getChildren()) { // Backend[*]
            if (node != null) {
                InnerNode child = new InnerNode((InnerNode) node);
                child.setKey(removeIndex(node.getKey()));
                consumer.accept(new Data(child));
            }
        }
    }

    /**
     * tbd.
     */
    public interface Resolvable {
        Map<String, Object> resolve();
    }

    /**
     * This data structure is used for managing application properties loaded.
     */
    public static class Data {

        private Node root;

        private Data() {
        }

        Data(Node root) {
            this.root = root;
        }

        public static Data empty() {
            return new Data();
        }

        public static Data of(Node root) {
            return new Data(root);
        }

        // key is always a path such as spring.caches.redis.type
        // value is the associated value
        Node insert(String key, Object value) {
            if (key == null) {
                throw new NullPointerException("key must not be null");
            }
            String[] paths = splitChainedKey(key);
            int noOfIterations = paths.length;
            while (noOfIterations-- > 0) {
                Iterator<String> it = Arrays.stream(paths).iterator();
                root = insert(root, it.next(), value, it);
            }
            return root;
        }

        @SuppressWarnings("ReturnCount")
        private Node insert(Node n, String key, Object value, Iterator<String> keys) {
            if (n == null) {
                return new LeafNode(key, value);
            }

            if (n instanceof InnerNode) {
                String k = keys.next();
                return ((InnerNode) n).addChild(insert(((InnerNode) n).findChild(k), k, value, keys));
            }

            if (n instanceof LeafNode) {
                if (keys.hasNext()) {
                    InnerNode innerNode = new InnerNode(4);
                    innerNode.setKey(n.getKey());
                    return innerNode.addChild(insert(null, keys.next(), value, keys));
                }

                ((LeafNode) n).setValue(value);
            }

            return n;
        }

        public Node getRoot() {
            return root;
        }

        public Optional<Node> find(String path) {
            return find(Arrays.stream(splitChainedKey(path)).iterator());
        }

        public Optional<Node> find(String... keys) {
            return find(Arrays.stream(keys).iterator());
        }

        public Optional<Node> find(Iterator<String> keys) {
            if (!keys.hasNext()) {
                return Optional.empty();
            }
            return Optional.ofNullable(find(root, keys.next(), keys));
        }

        @SuppressWarnings("ReturnCount")
        private Node find(Node node, String key, Iterator<String> remainingKeys) {
            if (!node.getKey().equals(key)) {
                return null;
            }
            if (node.getKey().equals(key) && !remainingKeys.hasNext()) {
                return node;
            }

            if (node instanceof InnerNode) {
                String k = remainingKeys.next();
                for (Node n : ((InnerNode) node).getChildren()) {
                    if (n == null) {
                        continue;
                    }
                    Node ref = find(n, k, remainingKeys);
                    if (ref != null) {
                        return ref;
                    }
                }
            }

            return null;
        }

        public void traverse(Visitable<Node> visitor) {
            traverse(root, visitor);
        }

        private void traverse(Node root, Visitable<Node> visitor) {
            if (root == null) {
                return;
            }
            visitor.accept(root);
            if (root instanceof InnerNode) {
                ((InnerNode) root).consume(n -> traverse(n, visitor));
            }
        }

        public <T> Optional<T> getValue(String chainedKey, Class<T> clazz) {
            return find(root.getKey() + chainedKey).map(Node::getValue).map(clazz::cast);
        }

        public boolean isEmpty() {
            return root == null;
        }

        public <T> T apply(Function<Data, T> fn) {
            return fn.apply(this);
        }

        public <A, B> B apply(Function<Data, A> a, Function<A, B> b) {
            return b.apply(a.apply(this));
        }

        public <C> C apply(NodeHandler<C> handler, C context) {
            handle(handler, context);
            return context;
        }

        public <C> void handle(NodeHandler<C> handler, C context) {
            traverse(node -> handler.handle(node, context));
        }

        private String[] splitChainedKey(String chainedKey) {
            return chainedKey.split("\\.");
        }

        @Override
        public String toString() {
            return Objects.requireNonNullElse(root, "()").toString();
        }
    }

}
