package entity;

import java.util.*;

public class Billboard implements Comparable<Billboard> {

    public double latitude, longitude;

    public String panelID;  // panelID = billboardID

    public int influence;   // number of routes get influenced

    public double charge;  //  cost  weeklyImpression / 3000

    private int advID;    // advertiser ID

    public double influencePerCharge;   // margInf / routes.size()

    private double margInf; // marginal influence gain

    private ArrayList<Route> routes;  // googleRouteID (used to store routes influenced by this billboard)

    private HashMap<Billboard, Double> overlapRatioList;

    public ArrayList<Double> distances; // the distance between the route and this billboard

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void addRoute(Route route) {
        this.routes.add(route);
    }

    public Billboard() {
        this.advID = -1;
        routes = new ArrayList<>();
        distances = new ArrayList<>();
        overlapRatioList = new HashMap<>();
    }

    /**
     * update the marginal influence of this billboard according to advID
     *
     * @param advID advertiser ID
     * @return return the marginal influence of adding this billboard
     */
    public double updateMargInf(int advID) {
        double margInf = 0.0;
        // sum of marginal influence of each route
        for (Route route : routes) {
            margInf += route.getMargInf(advID);
        }
        //return margInf;
        this.margInf = margInf;
        if (routes.size() > 0)
            //influencePerCharge = margInf / charge;
            influencePerCharge = margInf / this.influence;
        else
            influencePerCharge = 0.0;

        return influencePerCharge;
    }

    /**
     * assign this billboard to advID, update for all routes of this billboard
     *
     * @param advID advertiser ID
     */
    public void pick(int advID) {
        if (this.advID != -1) {
            System.out.println("This billboard has been selected!");
        }
        this.advID = advID;
        for (Route route : routes) {
            route.infByBillboard(advID);
        }
    }

    /**
     * umpick this billboard for the advertiser
     *
     * @return a route list that could be removed from the corresponding BillboardSet
     */
    public ArrayList<Route> unpick() {
        if (this.advID == -1)
            return null;
        ArrayList<Route> routeList = new ArrayList<>();
        for (Route route : routes) {
            if (route.unpickByBillboard(this.advID)) {
                routeList.add(route);
            }
        }
        this.advID = -1;
        return routeList;
    }

    public void reset() {
        this.influence = getRoutes().size();
        this.influencePerCharge = Math.ceil(influence / charge);
        this.advID = -1;
    }

    public void orderRoute() {
        if (distances.size() == 0)
            return;
        double current;
        for (int i = 0; i < distances.size() - 1; i++) {
            current = distances.get(i + 1);
            Route currentRoute = routes.get(i + 1);
            int preIndex = i;
            while (preIndex >= 0 && current < distances.get(preIndex)) {
                distances.set(preIndex + 1, distances.get(preIndex));
                routes.set(preIndex + 1, routes.get(preIndex));
                preIndex--;
            }
            distances.set(preIndex + 1, current);
            routes.set(preIndex + 1, currentRoute);
        }
    }

    public double getMargInf() {
        return margInf;
    }

    public int maxInf() {
        return routes.size();
    }

    public double getOverlapRatio(Billboard billboard) {
        if (overlapRatioList.containsKey(billboard))
            return overlapRatioList.get(billboard);
        else return 0;

    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();

        result.append("panelID : ").append(panelID).append("\n");
        result.append("influence : ").append(influence).append("\n");
        result.append("charge : ").append(charge).append("\n");
        result.append("influence per charge : ").append(influencePerCharge).append("\n");
        result.append("routes : ");

        for (Route route : routes)
            result.append(route.getRouteID()).append(", ");

        return result.toString();
    }


    // order by descending order (9 8 7 6 5 4 3 2 1)

    @Override
    public int compareTo(Billboard o) {

        double difference = o.influencePerCharge - influencePerCharge;

        //first compare influence/route number
        if (difference > 0)
            return 1;
        else if (difference < 0)
            return -1;
        else {
            //second compare influence
            difference = o.influence - influence;
            if (difference > 0)
                return 1;
            else if (difference < 0)
                return -1;
        }
        return 0;
    }
}
