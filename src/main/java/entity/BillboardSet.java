package entity;

import java.util.*;

public class BillboardSet {

    private ArrayList<Billboard> billboards;

    private Set<Route> routes; // the set of routes that this set of billboards can influence

    private int advID; //which advertiser own this result set

    private double influence; // the influence of this billboard

    private boolean infChanged; // whether influence need to be updated

    public boolean jump; // ignore this advertiser;

    public BillboardSet(int advID) {
        billboards = new ArrayList<>();
        routes = new HashSet<>();
        this.advID = advID;
        influence = 0.0;

        infChanged = false;
        jump = false;
    }

    public BillboardSet(BillboardSet billboardSet) {
        this(billboardSet.getAdvID());
        this.billboards.addAll(billboardSet.getAllBillboards());
        this.routes.addAll(billboardSet.getRoutes());
        this.influence = billboardSet.influence;
        jump = false;
        infChanged = true;
    }

    public void add(ArrayList<Billboard> billboards) {
        for (Billboard billboard : billboards) {
            add(billboard);
        }
    }

    public void add(Billboard billboard) {
        billboard.pick(this.advID);
        billboards.add(billboard);
        routes.addAll(billboard.getRoutes());
        infChanged = true;
    }

    public double infGain(Billboard billboard) {
        double infGain = 0.0;
        for (Route route : billboard.getRoutes()) {
            infGain += route.getMargInf(this.advID);
        }
        return infGain;
    }

    public double infLose(int billboardIndex) {
        Billboard billboard = billboards.get(billboardIndex);
        double infLose = 0.0;
        for (Route route : billboard.getRoutes()) {
            infLose += route.getLossInf(this.advID);
        }
        return infLose;
    }

    public void remove(Billboard billboard) {
        int billboardIndex = billboards.indexOf(billboard);
        ArrayList<Route> routeList = billboards.get(billboardIndex).unpick();
        billboards.remove(billboardIndex);
        if (routeList != null) {
            routes.removeAll(routeList);
        }
        infChanged = true; // if routeList.size >0
    }

    public void remove(int billboardIndex) {
        ArrayList<Route> routeList = billboards.get(billboardIndex).unpick();
        billboards.remove(billboardIndex);
        if (routeList != null) {
            routes.removeAll(routeList);
        }
        infChanged = true; // if routeList.size >0
    }

    public ArrayList<Billboard> getAllBillboards() {
        return billboards;
    }

    public Set<Route> getRoutes() {
        return routes;
    }

    /**
     * @param update rebuild billboard and route list if update = true
     * @return return influence
     */
    public double getInfluence(boolean update) {
        if (!update) {
            if (infChanged) {
                this.influence = routes.size();
                infChanged = false;
                return influence;

            } else
                return this.influence;
        } else {
            for (Route route : routes) {
                route.resetInf(advID);
            }
            routes = new HashSet<>();
            for (Billboard billboard : billboards) {
                billboard.reset();
                billboard.pick(advID);
                routes.addAll(billboard.getRoutes());
            }
            infChanged = true;
            getInfluence(false);
            return this.influence;
        }
    }

    public void release() {
        for (Route route : routes) {
            route.resetInf(this.advID);
        }
        for (Billboard billboard : billboards) {
            billboard.reset();
        }
        billboards = new ArrayList<>();
        routes = new HashSet<>();
        this.infChanged = false;
        this.influence = 0.0;
    }

    public double getCost() {
        double cost = 0;
        for (Billboard billboard : billboards)
            cost += billboard.charge;
        return cost;
    }

    public int getBillboardNumber() {
        return billboards.size();
    }

    public Billboard getBillboard(int index) {
        return billboards.get(index);
    }

    public int getAdvID() {
        return advID;
    }

    public void setAdvID(int advID) {
        this.advID = advID;
    }
}
