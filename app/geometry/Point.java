package geometry;

public class Point {
    private double latitude;
    private double longitude;

    private Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLat() {
        return latitude;
    }

    public double getLng() {
        return longitude;
    }

    // we don't want the users to use the Point constructor directly to avoid mixing up latitude and longitude
    public static class PointBuilder {
        private double latitude;
        private double longitude;
        public PointBuilder withLat(double latitude) {
            this.latitude = latitude;
            return this;
        }
        public PointBuilder withLng(double longitude) {
            this.longitude = longitude;
            return this;
        }
        public Point build() {
            return new Point(latitude, longitude);
        }
    }
}
