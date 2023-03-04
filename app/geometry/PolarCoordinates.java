package geometry;

public class PolarCoordinates {
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

}
