package xyz.yggdrazil.delaunay.voronoi.nodename.as3delaunay;

import xyz.yggdrazil.delaunay.geom.Point;

import java.util.Stack;

public final class Halfedge {

    private static Stack<Halfedge> _pool = new Stack();

    public static Halfedge create(Edge edge, LR lr) {
        if (_pool.size() > 0) {
            return _pool.pop().init(edge, lr);
        } else {
            return new Halfedge(edge, lr);
        }
    }

    public static Halfedge createDummy() {
        return create(null, null);
    }

    public Halfedge edgeListLeftNeighbor, edgeListRightNeighbor;
    public Halfedge nextInPriorityQueue;
    public Edge edge;
    public LR leftRight;
    public Vertex vertex;
    // the vertex's y-coordinate in the transformed Voronoi space V*
    public double ystar;

    public Halfedge(Edge edge, LR lr) {
        init(edge, lr);
    }

    private Halfedge init(Edge edge, LR lr) {
        this.edge = edge;
        leftRight = lr;
        nextInPriorityQueue = null;
        vertex = null;
        return this;
    }

    @Override
    public String toString() {
        return "Halfedge (leftRight: " + leftRight + "; vertex: " + vertex + ")";
    }

    public void dispose() {
        if (edgeListLeftNeighbor != null || edgeListRightNeighbor != null) {
            // still in EdgeList
            return;
        }
        if (nextInPriorityQueue != null) {
            // still in PriorityQueue
            return;
        }
        edge = null;
        leftRight = null;
        vertex = null;
        _pool.push(this);
    }

    public void reallyDispose() {
        edgeListLeftNeighbor = null;
        edgeListRightNeighbor = null;
        nextInPriorityQueue = null;
        edge = null;
        leftRight = null;
        vertex = null;
        _pool.push(this);
    }

    public boolean isLeftOf(Point p) {
        Site topSite;
        boolean rightOfSite, above, fast;
        double dxp, dyp, dxs, t1, t2, t3, yl;

        topSite = edge.getRightSite();
        rightOfSite = p.getX() > topSite.get_x();
        if (rightOfSite && this.leftRight == LR.LEFT) {
            return true;
        }
        if (!rightOfSite && this.leftRight == LR.RIGHT) {
            return false;
        }

        if (edge.getA() == 1.0) {
            dyp = p.getY() - topSite.get_y();
            dxp = p.getX() - topSite.get_x();
            fast = false;
            if ((!rightOfSite && edge.getB() < 0.0) || (rightOfSite && edge.getB() >= 0.0)) {
                above = dyp >= edge.getB() * dxp;
                fast = above;
            } else {
                above = p.getX() + p.getY() * edge.getB() > edge.getC();
                if (edge.getB() < 0.0) {
                    above = !above;
                }
                if (!above) {
                    fast = true;
                }
            }
            if (!fast) {
                dxs = topSite.get_x() - edge.getLeftSite().get_x();
                above = edge.getB() * (dxp * dxp - dyp * dyp)
                        < dxs * dyp * (1.0 + 2.0 * dxp / dxs + edge.getB() * edge.getB());
                if (edge.getB() < 0.0) {
                    above = !above;
                }
            }
        } else /* edge.b == 1.0 */ {
            yl = edge.getC() - edge.getA() * p.getX();
            t1 = p.getY() - yl;
            t2 = p.getX() - topSite.get_x();
            t3 = yl - topSite.get_y();
            above = t1 * t1 > t2 * t2 + t3 * t3;
        }
        return (this.leftRight == LR.LEFT) == above;
    }
}
