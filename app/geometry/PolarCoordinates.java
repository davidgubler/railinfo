package geometry;

public class PolarCoordinates {
    public static double bearingDegrees(double longitude1, double latitude1, double longitude2, double latitude2) {
        double lng1 = Math.toRadians(longitude1);
        double lat1 = Math.toRadians(latitude1);
        double lng2 = Math.toRadians(longitude2);
        double lat2 = Math.toRadians(latitude2);
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
