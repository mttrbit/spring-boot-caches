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

    private Tree tree;

    @BeforeEach
    public void init() {
        tree = Tree.empty();
    }

    @Test
    public void abc() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.b.d", "3");
        System.out.println(tree);
    }

    @Test
    public void countNodes() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.b.a", "3");
        assertThat(tree.apply((node, context) -> context.incrementAndGet(), new AtomicInteger()).get()).isEqualTo(5);
    }

    @Test
    public void collectLeafs() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.b.a", "3");
        Assertions.assertThat(tree.apply(TreeUtils.collectIf(TreeUtils.isLeaf()), new ArrayList<>()).size()).isEqualTo(3);
    }

    @Test
    public void handlingNodesWIthIndeces() {
        tree.insert("a.b[0].a", "1");
        tree.insert("a.b[0].b", "2");
        tree.insert("a.b[1].a", "3");
        tree.insert("a.b[1].b", "4");
        List<Node> nodes = tree.apply(TreeUtils.collectIf(TreeUtils.isArrayNode()), new ArrayList<>());
        assertThat(nodes.size()).isEqualTo(2);
        assertThat(nodes.toString()).isEqualTo("[(b[0],[(a=1), (b=2)]), (b[1],[(a=3), (b=4)])]");
    }
    @Test
    public void traverse() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.b.d", "3");
        Properties props = tree.apply((node, context) -> {
            if (node instanceof LeafNode) {
                context.put(
                        StreamSupport.stream(node.path(), false).collect(Collectors.joining(".")),
                        ((LeafNode) node).getValue()
                );
            }
        }, new Properties());
        assertThat(props.size()).isEqualTo(2);
    }

    @Test
    public void findFirstSubTree() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.e.d", "3");
        Tree subTree = tree.apply(TreeUtils.subtree(TreeUtils.hasKey("d")));
        assertThat(subTree.find("d").isPresent()).isTrue();
        assertThat(subTree.find("d").get().toString()).isEqualTo("(d=2)");
    }

    @Test
    public void findSubTrees() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.e.d", "3");
        List<Tree> trees = tree.apply(TreeUtils.subtrees(TreeUtils.hasKey("d")));
        assertThat(trees.size()).isEqualTo(2);
        assertThat(trees.get(0).toString()).isEqualTo("(d=2)");
        assertThat(trees.get(1).toString()).isEqualTo("(d=3)");
    }

    @Test
    public void findNode() {
        tree.insert("a.b.g.d", "2");
        assertThat(tree.find("a.b.g.d").isPresent()).isTrue();
    }

    @Test
    public void findNode2() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.g.d", "2");
        tree.insert("a.e.d", "3");
        Assertions.assertThat(tree.apply(TreeUtils.subtree(TreeUtils.hasChild(new LeafNode("c", "1"))), TreeUtils.valueOf("b.g.d"))).isEqualTo("2");
    }

    @Test
    public void findAll() {
        tree.insert("a.b.c", "1");
        tree.insert("a.b.d", "2");
        tree.insert("a.e.d", "3");
        tree.insert("a.f.c", "1");
        Assertions.assertThat(tree.apply(TreeUtils.collectIf(TreeUtils.hasChild(new LeafNode("c", "1"))), new ArrayList<>()).size()).isEqualTo(2);
    }

    @Test
    public void findAll2() {
        tree.insert("a.a.t", "1");
        tree.insert("a.b.t", "2");
        tree.insert("a.c.t", "3");
        Map<String, String> caches = new HashMap<>();
        tree.apply(TreeUtils.subtrees(TreeUtils.hasChild("t"))).forEach(t -> t.apply((n, c) -> {
            if (n.isLeaf()) {
                String key = n.getParent().getKey();
                String cache = String.valueOf(n.getValue());
                c.put(key, cache);
            }
        }, caches));
        assertThat(caches.size()).isEqualTo(3);
    }
}