package entities.realized;

import entities.Stop;

import java.util.Comparator;

public class RealizedPassIntermediateComparator implements Comparator<RealizedPass> {

    private Stop stop1;
    private double pos;

    public RealizedPassIntermediateComparator(Stop stop1, double pos) {
        this.stop1 = stop1;
        this.pos = pos;
    }

    @Override
    public int compare(RealizedPass realizedPass, RealizedPass t1) {
        return realizedPass.getIntermediate(realizedPass.isForward(stop1), pos).compareTo(t1.getIntermediate(t1.isForward(stop1), pos));
    }
}
