package entities.api;

import java.util.List;
import java.util.Map;

public class ApiEdgeTraffic {
    private Map<String, Double> edges;

    private List<ApiEdgePass> passes;

    public ApiEdgeTraffic(Map<String, Double> edges, List<ApiEdgePass> passes) {
        this.edges = edges;
        this.passes = passes;
    }
}
