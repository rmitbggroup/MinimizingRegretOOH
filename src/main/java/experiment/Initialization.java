package experiment;

import java.util.*;

import algorithms.GreedyFunction;
import entity.*;
import fileIO.finalResult.FinalResultReader;

public class Initialization {

    double totalInf;
    private ArrayList<Range> rangeList;
    private static double lambda = 50;
    private List<Billboard> billboards;
    private List<Route> routes;

    private ExpSetting exp, preExp;

    public Initialization() {
        System.out.println("Start Initialisation");
        rangeList = new ArrayList<>();
    }

    public void setRanges(String rangeList) {
        this.rangeList = new ArrayList<>();
        if (rangeList.length() == 0)
            return;


        double longitude1, latitude1, longitude2, latitude2;
        try {
            String[] rangeCoordinate = rangeList.split("/");

            for (int i = 0; i < rangeCoordinate.length; i += 4) {
                longitude1 = Double.parseDouble(rangeCoordinate[i]);
                latitude1 = Double.parseDouble(rangeCoordinate[i + 1]);
                longitude2 = Double.parseDouble(rangeCoordinate[i + 2]);
                latitude2 = Double.parseDouble(rangeCoordinate[i + 3]);
                setRange(longitude1, latitude1, longitude2, latitude2);
            }
        } catch (Exception e) {
            System.out.println("illegal coordinates input");
        }

    }

    private void setRange(double longitude1, double latitude1, double longitude2, double latitude2) {
        rangeList.add(new Range(longitude1, latitude1, longitude2, latitude2));
    }

    private ArrayList<Billboard> generateBillboards() {
        ArrayList<Billboard> billboards = new ArrayList<>();

        boolean ifInRange;
        for (Billboard billboard : this.billboards) {
            if (rangeList.size() > 0) {
                ifInRange = false;
                for (Range range : rangeList) {
                    if (range.ifContain(billboard)) {
                        ifInRange = true;
                        break;
                    }
                }
            } else
                ifInRange = true; // no range has been set

            if (!ifInRange)
                continue;

            Billboard newBillboard = new Billboard();
            newBillboard.panelID = billboard.panelID;
            newBillboard.longitude = billboard.longitude;
            newBillboard.latitude = billboard.latitude;
            newBillboard.charge = billboard.charge * Setting.beta;

            for (int i = 0; i < billboard.getRoutes().size(); i++) {
                if (lambda >= billboard.distances.get(i)) {
                    newBillboard.addRoute(billboard.getRoutes().get(i));
                } else
                    break;
            }

            if (newBillboard.getRoutes() != null && newBillboard.getRoutes().size() > 10) {
                newBillboard.influence = newBillboard.getRoutes().size();
                billboards.add(newBillboard);
            }
        }

        for (Route route : routes) {
            route.resetInf();
        }

        return billboards;
    }

    public void reload() {
        if (preExp != null) {
            if (preExp.multiBillboard == exp.multiBillboard &&
                    preExp.infProb == exp.infProb &&
                    preExp.trajPer == exp.trajPer &&
                    preExp.beta == exp.beta &&
                    preExp.lambda == exp.lambda &&
                    preExp.range.equals(exp.range)
            ) {
                resetData();
                return;
            }
        }
        preExp = exp;
        lambda = exp.lambda;
        FinalResultReader finalResultReader = new FinalResultReader();
        finalResultReader.reload();
        this.billboards = finalResultReader.getBillboards();
        this.routes = finalResultReader.getRoutes();
        this.billboards = generateBillboards(); // generate billboards based on the query range list and lambda
        GreedyFunction greedyFunction = new GreedyFunction();
        greedyFunction.order(this.billboards);

        System.out.println("total number of billboards: " + this.billboards.size());
        //deleteBillboardPre();
        totalInf = 0;
        for (Billboard billboard : billboards) {
            totalInf += billboard.getRoutes().size();
        }
    }

    private void resetData() {
        for (Billboard billboard : billboards) {
            billboard.reset();
        }
        for (Route route : routes) {
            route.resetAdvNum();
        }
    }

    public ArrayList<Billboard> getBillboards() {

        ArrayList<Billboard> billboards = new ArrayList<>(this.billboards);

        for (Billboard billboard : billboards) {
            billboard.reset();
        }

        for (Route route : routes) {
            route.resetAdvNum();
            //route.resetInf();
        }

        return billboards;
    }

    public int getRouteNum() {
        return routes.size();
    }

    public void setExp(ExpSetting exp) {
        this.exp = exp;
    }

    private class Range {

        private double longitude1;
        private double latitude1;
        private double longitude2;
        private double latitude2;

        Range(double longitude1, double latitude1, double longitude2, double latitude2) {
            if (longitude1 < longitude2) {
                this.longitude1 = longitude1;
                this.longitude2 = longitude2;
            } else {
                this.longitude1 = longitude2;
                this.longitude2 = longitude1;
            }
            if (latitude1 < latitude2) {
                this.latitude1 = latitude1;
                this.latitude2 = latitude2;
            } else {
                this.latitude1 = latitude2;
                this.latitude2 = latitude1;
            }
        }

        boolean ifContain(Billboard billboard) {

            if (longitude1 <= billboard.longitude && billboard.longitude <= longitude2) {
                return latitude1 <= billboard.latitude && billboard.latitude <= latitude2;
            }
            return false;
        }
    }


}
