package nz.sodium;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Node implements Comparable<Node> {
    public final static Node NULL = new Node(Long.MAX_VALUE);

    Node(long rank) {
        this.rank = rank;
    }

    public static class Target {
        Target(TransactionHandler<Unit> action, Node node) {
            this.action = new WeakReference<>(action);
            this.node = node;
        }

        final WeakReference<TransactionHandler<Unit>> action;
        final Node node;
    }

    private long rank;
    List<Target> listeners = new ArrayList<>();

    /**
     * @return true if any changes were made.
     */
    boolean linkTo(TransactionHandler<Unit> action, Node target, Target[] outTarget) {
        boolean changed = target.ensureBiggerThan(rank, new HashSet<>());
        Target t = new Target(action, target);
        listeners.add(t);
        outTarget[0] = t;
        return changed;
    }

    void unlinkTo(Target target) {
        listeners.remove(target);
    }

    private boolean ensureBiggerThan(long limit, Set<Node> visited) {
        if (rank > limit || visited.contains(this))
            return false;

        visited.add(this);
        rank = limit + 1;
        for (Target l : listeners)
            l.node.ensureBiggerThan(rank, visited);
        visited.remove(this);
        return true;
    }

    @Override
    public int compareTo(Node o) {
        return Long.compare(rank, o.rank);
    }
}
