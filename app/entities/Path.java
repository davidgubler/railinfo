package entities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Path {
    private List<Edge> edges = null;
    private long duration = 0l;

    public Path() {
        edges = new LinkedList<>();
    }

    public Path(Edge edge, Path path) {
        List<Edge> edges = new LinkedList<>();
        edges.add(edge);
        edges.addAll(path.edges);
        this.edges = Collections.unmodifiableList(edges);
        duration = edge.getTypicalTime() + path.duration;
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

    public String toString(Stop start) {
        String str = start.getName();
        for (Edge edge : edges) {
            start = edge.getDestination(start);
            str += " -> " + start;
        }
        return str;
    }
}