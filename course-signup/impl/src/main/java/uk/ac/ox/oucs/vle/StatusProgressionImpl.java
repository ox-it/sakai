package uk.ac.ox.oucs.vle;

import uk.ac.ox.oucs.vle.CourseSignupService.Status;

import java.util.*;

/**
 * This class models the progression through the statuses.
 * This is a graph of directed nodes through which normal flow can occur.
 * Next being forwards and previous being backwards.
 */
public class StatusProgressionImpl implements StatusProgression {

    private Map<Status, Node> statuses = new HashMap<>();

    private class Node {
        private Status status;
        private Set<Node> next = new HashSet<>();
        private Set<Node> previous = new HashSet<>();

        private Node(Status status, Node... previous) {
            this.status = status;
            for (Node node: previous) {
                addPrevious(node);
            }
            if (statuses.containsKey(status)) {
                throw new IllegalArgumentException("Status is already in the progression: "+ status);
            }
            statuses.put(status, this);
        }

        public void addPrevious(Node other) {
            this.previous.add(other);
            other.next.add(this);
        }
    }

    public StatusProgressionImpl() {
        // We build the graph up start at the beginning.
        Node pending = new Node(Status.PENDING);
        Node waiting = new Node(Status.WAITING, pending);
        Node accepted = new Node(Status.ACCEPTED, waiting, pending);
        Node approved = new Node(Status.APPROVED, accepted);
        Node confirmed = new Node(Status.CONFIRMED, approved);
        Node rejected = new Node(Status.REJECTED, waiting, pending, accepted);
        Node withdrawn = new Node(Status.WITHDRAWN, pending);
    }

    @Override
    public Collection<Status> next(Status status) {
        Node node = statuses.get(status);
        if (node != null) {
            return toStatus(node.next);
        }
        return null;
    }

    @Override
    public Collection<Status> previous(Status status) {
        Node node = statuses.get(status);
        if (node != null) {
            return toStatus(node.previous);
        }
        return null;
    }

    protected List<Status> toStatus(Collection<Node> nodes) {
        List<Status> statuses = new ArrayList<>(nodes.size());
        for (Node node: nodes) {
            statuses.add(node.status);
        }
        return statuses;
    }
}
