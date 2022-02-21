package spring.caches.backend.properties.tree;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class TreeTest {

    private CachesProperties.Data data;

    @BeforeEach
    public void init() {
        data = CachesProperties.Data.empty();
    }

    @Test
    public void abc() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.b.d", "3");
        System.out.println(data);
    }

    @Test
    public void countNodes() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.b.a", "3");
        assertThat(data.apply((node, context) -> context.incrementAndGet(), new AtomicInteger()).get()).isEqualTo(5);
    }

    @Test
    public void collectLeafs() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.b.a", "3");
        Assertions.assertThat(data.apply(CachePropertiesUtils.collectIf(CachePropertiesUtils.isLeaf()), new ArrayList<>()).size()).isEqualTo(3);
    }

    @Test
    public void handlingNodesWIthIndeces() {
        data.insert("a.b[0].a", "1");
        data.insert("a.b[0].b", "2");
        data.insert("a.b[1].a", "3");
        data.insert("a.b[1].b", "4");
        List<Node> nodes = data.apply(CachePropertiesUtils.collectIf(CachePropertiesUtils.isArrayNode()), new ArrayList<>());
        assertThat(nodes.size()).isEqualTo(2);
        assertThat(nodes.toString()).isEqualTo("[(b[0],[(a=1), (b=2)]), (b[1],[(a=3), (b=4)])]");
    }

    @Test
    public void traverse() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.b.d", "3");
        Properties props = data.apply((node, context) -> {
            if (node instanceof LeafNode) {
                context.put(
                        StreamSupport.stream(node.path(), false).collect(Collectors.joining(".")),
                        node.getValue()
                );
            }
        }, new Properties());
        assertThat(props.size()).isEqualTo(2);
    }

    @Test
    public void findFirstSubTree() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.e.d", "3");
        CachesProperties.Data subTree = data.apply(CachePropertiesUtils.subtree(CachePropertiesUtils.hasKey("d")));
        assertThat(subTree.find("d").isPresent()).isTrue();
        assertThat(subTree.find("d").get().toString()).isEqualTo("(d=2)");
    }

    @Test
    public void findSubTrees() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.e.d", "3");
        List<CachesProperties.Data> trees = data.apply(CachePropertiesUtils.subtrees(CachePropertiesUtils.hasKey("d")));
        assertThat(trees.size()).isEqualTo(2);
        assertThat(trees.get(0).toString()).isEqualTo("(d=2)");
        assertThat(trees.get(1).toString()).isEqualTo("(d=3)");
    }

    @Test
    public void findNode() {
        data.insert("a.b.g.d", "2");
        assertThat(data.find("a.b.g.d").isPresent()).isTrue();
    }

    @Test
    public void findNode2() {
        data.insert("a.b.c", "1");
        data.insert("a.b.g.d", "2");
        data.insert("a.e.d", "3");
        Assertions.assertThat(data.apply(CachePropertiesUtils.subtree(CachePropertiesUtils.hasChild(new LeafNode("c", "1"))), CachePropertiesUtils.valueOf("b.g.d"))).isEqualTo("2");
    }

    @Test
    public void findAll() {
        data.insert("a.b.c", "1");
        data.insert("a.b.d", "2");
        data.insert("a.e.d", "3");
        data.insert("a.f.c", "1");
        Assertions.assertThat(data.apply(CachePropertiesUtils.collectIf(CachePropertiesUtils.hasChild(new LeafNode("c", "1"))), new ArrayList<>()).size()).isEqualTo(2);
    }

    @Test
    public void findAll2() {
        data.insert("a.a.t", "1");
        data.insert("a.b.t", "2");
        data.insert("a.c.t", "3");
        Map<String, String> caches = new HashMap<>();
        data.apply(CachePropertiesUtils.subtrees(CachePropertiesUtils.hasChild("t"))).forEach(t -> t.apply((n, c) -> {
            if (n.isLeaf()) {
                String key = n.getParent().getKey();
                String cache = String.valueOf(n.getValue());
                c.put(key, cache);
            }
        }, caches));
        assertThat(caches.size()).isEqualTo(3);
    }
}