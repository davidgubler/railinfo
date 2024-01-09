package controllers;

import configs.GtfsConfig;
import entities.Edge;
import entities.Stop;
import entities.User;
import play.mvc.Controller;
import utils.NotAllowedException;
import utils.NotFoundException;

public class GtfsController extends Controller {
    protected void check(User user, GtfsConfig gtfs, Stop stop) {
        check(user, gtfs);
        if (stop == null) {
            throw new NotFoundException("Stop");
        }
    }

    protected void check(User user, GtfsConfig gtfs, Edge edge) {
        check(user, gtfs);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
    }

    protected void check(GtfsConfig gtfs, Edge edge) {
        check(gtfs);
        if (edge == null) {
            throw new NotFoundException("Edge");
        }
    }

    protected void check(User user, GtfsConfig gtfs) {
        check(user);
        check(gtfs);
    }

    protected void check(User user) {
        if (user == null) {
            throw new NotAllowedException();
        }
    }

    protected void check(GtfsConfig gtfs) {
        if (gtfs == null || gtfs.getDatabase() == null) {
            throw new NotFoundException("Database");
        }
    }

    protected void checkDbOptional(User user, GtfsConfig gtfs) {
        if (gtfs == null) {
            throw new NotFoundException("Database");
        }
        check(user);
    }
}
