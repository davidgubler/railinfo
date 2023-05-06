package entities;

import java.util.*;

public class Path {
    private List<Edge> edges = null;
    private long duration = 0l;

    public Path() {
        edges = new LinkedList<>();
    }

    public Path(Edge edge) {
        edges = Arrays.asList(edge);
        duration = edge.getTypicalTime();
    }

    public Path(Path path, Edge edge) {
        List<Edge> edges = new ArrayList<>();
        edges.addAll(path.edges);
        edges.add(edge);
        this.edges = Collections.unmodifiableList(edges);
        duration = path.duration + edge.getTypicalTime();
    }

    public Path getReverse() {
        Path reversePath = new Path();
        reversePath.edges = new LinkedList<>(edges);
        Collections.reverse(reversePath.edges);
        reversePath.duration = duration;
        return reversePath;
    }

    public long getDuration() {
        return duration;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Stop getDestination(Stop from) {
        if (edges.isEmpty()) {
            return null;
        }
        if (edges.size() == 1) {
            return edges.get(0).getDestination(from);
        }
        Edge secondToLast = edges.get(edges.size() - 2);
        Edge last = edges.get(edges.size() - 1);
        if (secondToLast.getStop1Id().equals(last.getStop1Id()) || secondToLast.getStop2Id().equals(last.getStop1Id())) {
            return last.getStop2();
        }
        return last.getStop1();
    }

    public String toString(Stop start) {
        String str = start.getName();
        for (Edge edge : edges) {
            start = edge.getDestination(start);
            str += " -> " + start.getName();
        }
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return duration == path.duration && Objects.equals(edges, path.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edges, duration);
    }
}
