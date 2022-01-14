package spring.caches.backend.properties.tree;

abstract class AbstractNode<T extends AbstractNode<T>> implements Visitable<Visitor<T>>, Node {

    protected Node parent;

    AbstractNode() {
        // intentionally left blank
    }

    AbstractNode(Node node) {
        this.parent = node.getParent();
    }

    @Override
    public abstract String getKey();

    @Override
    public boolean hasParent() {
        return parent != null;
    }

    @Override
    public Node getParent() {
        return parent;
    }

    protected void setParent(Node node) {
        parent = node;
    }
}
