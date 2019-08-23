package org.twak.tweed.gen;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Junction extends Point3d {
    public List<Street> streets;

    public Junction(Point3d p1) {
        super(p1);
        streets = new ArrayList<>();
    }

    public Street addStreet(Junction p) {
        Street s = new Street(this, p);
        streets.add(s);
        return s;
    }

    public Street addStreet(Street s) {
        streets.add(s);
        return s;
    }

    public List<Street> getOutwardsGoingStreets() {
        List<Street> out = new ArrayList<>();
        for (Street s : streets) {
            Street newStreet = new Street(this, s.getOther(this));
            newStreet.width = s.width;
            out.add(newStreet);
        }
        order(out);

        return out;
    }

    public List<Street> getInwardsGoingStreets() {
        List<Street> in = new ArrayList<>();
        for (Street s : streets) {
            Street newStreet = new Street(s.getOther(this), this);
            newStreet.width = s.width;
            in.add(newStreet);
        }
        order(in);

        return in;
    }

    public void order() {
        order(streets);
    }

    public static void order (List<Street> streets){
        Street s1 = streets.get(0);
        for (Street s2 : streets) {
            // cross product
            Vector3f cross = new Vector3f();
            cross.cross(s1.getVector(), s2.getVector());

            double angle;
            if (cross.y > 0) {
                angle = 2*Math.PI - s1.getVector().angle(s2.getVector());
            } else {
                angle = s1.getVector().angle(s2.getVector());
            }
            s2.angle = angle;
        }

        Collections.sort(streets);
    }

    public boolean hasStreetTo(Junction v1) {
        for (Street s : streets) {
            if (s.getP1() == this && s.getP2() == v1)
                return true;

            if (s.getP2() == this && s.getP1() == v1)
                return true;
        }

        return false;
    }
}
