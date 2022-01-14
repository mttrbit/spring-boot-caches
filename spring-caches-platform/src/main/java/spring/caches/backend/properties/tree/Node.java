package spring.caches.backend.properties.tree;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;

/**
 * tbd.
 */
public interface Node {

    Node getParent();

    boolean hasParent();

    boolean isLeaf();

    String getKey();

    Object getValue();

    default Spliterator<String> path() {
        Deque<String> path = new ArrayDeque<>();
        path.push(this.getKey());
        Node ref = this;
        while ((ref = ref.getParent()) != null) {
            path.push(ref.getKey());
        }

        return path.stream().spliterator();
    }
}
