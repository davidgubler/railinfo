package geometry;

public class PolarCoordinates {
    private static final double R = 6371.0088;

    public static double bearingDegrees(Point point1, Point point2) {
        double lng1 = Math.toRadians(point1.getLng());
        double lat1 = Math.toRadians(point1.getLat());
        double lng2 = Math.toRadians(point2.getLng());
        double lat2 = Math.toRadians(point2.getLat());
        double lngDiff = lng2 - lng1;

        double X = Math.cos(lat2) * Math.sin(lngDiff);
        double Y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lngDiff);
        return (Math.toDegrees(Math.atan2(X,Y)) + 360) % 360;
    }

    public static double bearingDiff(double bearing1, double bearing2) {
        double t = Math.abs(bearing1 - bearing2) % 360;
        return t > 180 ? 360 - t : t;
    }

    public static double distanceKm(Point point1, Point point2) {
        Double latDistance = Math.toRadians(point2.getLat()-point1.getLat());
        Double lonDistance = Math.toRadians(point2.getLng()-point1.getLng());
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
            Math.cos(Math.toRadians(point1.getLat())) * Math.cos(Math.toRadians(point2.getLat())) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c;
    }

    public static Point goNorth(Point startingPoint, double distanceKm) {
        double newLat = startingPoint.getLat() + 180 * distanceKm / (Math.PI * R);
        return new Point.PointBuilder().withLat(newLat).withLng(startingPoint.getLng()).build();
    }

    public static Point goEast(Point startingPoint, double distanceKm) {
        double newLng = startingPoint.getLng() + 180 * distanceKm / (Math.PI * R * Math.cos(Math.toRadians(startingPoint.getLat())));
        return new Point.PointBuilder().withLat(startingPoint.getLat()).withLng(newLng).build();
    }

    public static double distanceFromEdgeKm(Point point, Point linePoint1, Point linePoint2) {
        // calculate how far a point is from a line (given by two points). This is a 2D calculation only and doesn't account for the curvature of the earth.
        // Calculate triangle area using Heron's formula
        double a = distanceKm(point, linePoint1);
        double b = distanceKm(linePoint1, linePoint2);
        double c = distanceKm(linePoint2, point);
        double s = (a + b + c)/2.0;
        double area = Math.sqrt(s*(s - a)*(s - b)*(s - c));
        // Get distance from triangle area
        double distance = 2.0 * area / b;
        return distance;
    }
}
