package entities.api;

public class ApiEdgePass {
    private final String edge;

    private final boolean forward;

    private final String localTime;

    private final long epochTime;

    private final String shortName;

    private final String tripShortName;

    private final String tripBegins;

    private final String tripEnds;

    public ApiEdgePass(String edge, boolean forward, String localTime, long epochTime, String shortName, String tripShortName, String tripBegins, String tripEnds) {
        this.edge = edge;
        this.forward = forward;
        this.localTime = localTime;
        this.epochTime = epochTime;
        this.shortName = shortName;
        this.tripShortName = tripShortName;
        this.tripBegins = tripBegins;
        this.tripEnds = tripEnds;
    }
}
