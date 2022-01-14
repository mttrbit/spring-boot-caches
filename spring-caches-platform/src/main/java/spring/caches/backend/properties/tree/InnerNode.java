package spring.caches.backend.properties.tree;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class InnerNode extends AbstractNode<InnerNode> {

    private Node[] children;
    private int noOfChildren;
    private String key;
    private int nodeSize;

    InnerNode(int size) {
        nodeSize = size;
        children = new Node[nodeSize + 1];
    }

    // copy ctor
    InnerNode(InnerNode node) {
        super(node);
        nodeSize = node.nodeSize;
        noOfChildren = node.noOfChildren;
        children = new Node[nodeSize + 1];
        System.arraycopy(node.children, 0, this.children, 0, Math.max(node.noOfChildren, nodeSize));
    }

    Node[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    Node findChild(String key) {
        for (int i = 0; i < noOfChildren; i++) {
            if (children[i].getKey().equals(key)) {
                return children[i];
            }
        }
        return null;
    }

    boolean isFull() {
        return noOfChildren >= nodeSize;
    }

    boolean hasChild(String key) {
        for (int i = 0; i < noOfChildren; i++) {
            if (children[i].getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    boolean hasChild(Node node) {
        for (int i = 0; i < noOfChildren; i++) {
            if (children[i].equals(node)) {
                return true;
            }
        }
        return false;
    }

    Node addChild(Node node) {
        if (node instanceof AbstractNode<?>) {
            ((AbstractNode<?>) node).setParent(this);
        }
        int pos = getIndexOf(node.getKey());
        if (pos == -1) {
            if (isFull()) {
                nodeSize = nodeSize + 4;
                Node[] cs = new Node[nodeSize + 1];
                System.arraycopy(children, 0, cs, 0, noOfChildren);
                this.children = cs;
            }
            children[noOfChildren] = node;
            noOfChildren++;
        } else {
            children[pos] = node;
        }

        return this;
    }

    int getIndexOf(String key) {
        for (int i = 0; i < noOfChildren; i++) {
            if (children[i].getKey().equals(key)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public String getKey() {
        return key;
    }

    void setKey(String key) {
        this.key = key;
    }

    // Inner nodes do not have any value, hence we return an empty shell.
    @Override
    public Object getValue() {
        return new Object();
    }

    @Override
    public void accept(Visitor<InnerNode> visitor) {
        visitor.visit(this);
    }

    public void consume(Consumer<Node> consumer) {
        Arrays.stream(children).forEach(consumer);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InnerNode innerNode = (InnerNode) o;
        return Objects.equals(key, innerNode.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "(" + key + "," + Arrays.stream(children)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]")) + ")";
    }
}
