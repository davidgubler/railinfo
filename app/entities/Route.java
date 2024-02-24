package entities;

import configs.GtfsConfig;

public interface Route {
    String getRouteId();

    String getAgencyId();

    String getShortName();

    String getLongName();

    String getDesc();

    Integer getType();

    String getProduct();

    String getLineName();

    GtfsConfig getSourceGtfs();
}
