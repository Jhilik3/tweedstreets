package org.twak.tweed.gen;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class Street implements Comparable<Street> {

    private Junction p1, p2;
    public double angle;
    public Point3d c1, c2, c3, c4;
    public int width;

    public Street(Junction point1, Junction point2) {
        p1 = point1;
        p2 = point2;
        width = (int)(Math.random() * 4) + 2;
    }

    public Point3d getP1() {
        return p1;
    }

    public Point3d getP2() {
        return p2;
    }

    public Junction getJ1() {
        return p1;
    }

    public Junction getJ2() {
        return p2;
    }

    public double getLength() {
        return p1.distance(p2);
    }

    public Vector3f getVector() {
        return new Vector3f((float)(p1.x-p2.x), (float)(p1.y-p2.y), (float)(p1.z-p2.z));
    }

    public int compareTo(Street s) {
        return Double.compare(this.angle, s.angle);
    }

    public Junction getOther(Junction p) {
        if (p == p1) {
            return p2;
        } else if (p == p2) {
            return p1;
        } else {
            throw new Error("bad junction");
        }
    }

    private Street findSameStartEnd (Street real, List<Street> temp) {
        for (Street t : temp) {
            if (t.p1.equals(real.p1) && t.p2.equals(real.p2 ))
                return t;
            if (t.p2.equals(real.p1) && t.p1.equals(real.p2 ))
                return t;
        }

        return null;
    }

    public List<Street> createTempStreets(List<Street> tempP1, List<Street> tempP2) {
        List<Street> streets = new ArrayList<>();
        Street cur, fr, fl, br, bl;

        cur = this;
        streets.add(0, cur);

        int indexFront = tempP1.indexOf( findSameStartEnd(cur, tempP1) );
        fr = tempP1.get((indexFront+1) % tempP1.size());

        // don't add if it's the same as this street
        if (indexFront != tempP1.indexOf(fr)) {
            // change direction
            if (! fr.getP1().equals  ( cur.getP1())) {
                fr.changeDirection();
                Junction.order(tempP1);
            }
            streets.add(1, fr);
        } else {
            streets.add(1, null);
        }

        int flIndex = ((indexFront - 1) + p1.streets.size()) % p1.streets.size();
        // check if the same as this street
        if (indexFront != flIndex) {
            fl = tempP1.get(flIndex);
            // change direction
            if (!fl.getP1().equals( cur.getP1())) {
                fl.changeDirection();
                Junction.order(tempP1);
            }
            // check if same as fr
            if (tempP1.indexOf(fr) != tempP1.indexOf(fl))
                streets.add(2, fl);
            else
                streets.add(2, null);
        } else {
            streets.add(2, null);
        }

        int indexBack = tempP2.indexOf(findSameStartEnd(cur, tempP2));
        br = tempP2.get((indexBack+1) % tempP2.size());

        if (indexBack != tempP2.indexOf(br)) {
            if (!br.getP2() .equals ( cur.getP2()) ) {
                br.changeDirection();
                Junction.order(tempP2);
            }
            streets.add(3, br);
        } else {
            streets.add(3, null);
        }

        int blIndex = ((indexBack - 1) + p2.streets.size()) % p2.streets.size();
        if (indexBack != blIndex) {
            bl = tempP2.get(blIndex);
            if (!bl.getP2().equals ( cur.getP2()) ) {
                bl.changeDirection();
                Junction.order(tempP2);
            }
            if (tempP2.indexOf(br) != tempP2.indexOf(bl))
                streets.add(4, bl);
            else
                streets.add(4, null);
        } else {
            streets.add(4, null);
        }

        for (Street s : streets) {
            if (s != null)
                s.corners();
        }

        return streets;
    }

    public void corners() {
        // perpendicular vector
        Vector3f vector = new Vector3f((float)-(p2.z-p1.z), (float)(p2.y-p1.y), (float)(p2.x-p1.x));

        Vector3f v = new Vector3f(vector.x*(width/vector.length()), vector.y*(width/vector.length()), vector.z*(width/vector.length()));

        c1 = new Point3d((p1.x+v.x), (p1.y+v.y), (p1.z+v.z));
        c2 = new Point3d((p1.x-v.x), (p1.y-v.y), (p1.z-v.z));
        c3 = new Point3d((p2.x+v.x), (p2.y+v.y), (p2.z+v.z));
        c4 = new Point3d((p2.x-v.x), (p2.y-v.y), (p2.z-v.z));
    }

    public Point3d getC1() {
        return c1;
    }

    public Point3d getC2() {
        return c2;
    }

    public Point3d getC3() {
        return c3;
    }

    public Point3d getC4() {
        return c4;
    }

    public Point3d intersect(Point3d p1, Point3d p2, Point3d p3, Point3d p4) {
        double denom = (p1.x - p2.x)*(p3.z - p4.z) - (p1.z - p2.z)*(p3.x - p4.x);
        double numx = ((p1.x*p2.z - p1.z*p2.x)*(p3.x-p4.x)) - ((p1.x-p2.x)*(p3.x*p4.z - p3.z*p4.x));
        double numy = (p1.x*p2.z - p1.z*p2.x)*(p3.z-p4.z) - (p1.z-p2.z)*(p3.x*p4.z - p3.z*p4.x);

		Point3d intersection = new Point3d(numx/denom, 0, numy/denom);

        Point3d fix = new Point3d((p3.x+p1.x)/2, (p3.y+p1.y)/2, (p3.z+p1.z)/2);
		// for errors when the streets are parallel
		if (denom == 0) {
		    return fix;
        }

        // if intersection is too far away
		if (intersection.distance(fix) > 6) {
		    return fix;
        }
		return intersection;
    }

    private void changeDirection() {
        Junction one = this.getJ2();
        Junction two = this.getJ1();

        this.p1 = one;
        this.p2 = two;
    }
}
