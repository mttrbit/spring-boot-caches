package spring.caches.backend.properties.tree;

import java.util.Objects;

class LeafNode extends AbstractNode<LeafNode> {
    private final String key;
    private Object value;

    LeafNode(String key, Object value) {
        this.value = value;
        this.key = key;
    }

    // copy ctor
    LeafNode(LeafNode node) {
        super(node);
        this.value = node.value;
        this.key = node.key;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public String getKey() {
        return key;
    }

    void setValue(Object value) {
        this.value = value;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void accept(Visitor<LeafNode> visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LeafNode leafNode = (LeafNode) o;
        return Objects.equals(key, leafNode.key) && Objects.equals(value, leafNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return String.format("(%s=%s)", key, value);
    }
}
