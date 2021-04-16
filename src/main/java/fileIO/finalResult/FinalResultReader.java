package fileIO.finalResult;

import entity.*;
import fileIO.MyFileReader;

import java.util.*;

public class FinalResultReader {

    private long start;

    private static List<String> panelIDs;
    private static List<Integer> weeklyImpressions;
    private static Set<Integer> routeIDSet;
    private static ArrayList<Double> lantitudeList;
    private static ArrayList<Double> longitudeList;
    private static List<List<Integer>> routeIDsOfBillboards;   // { {routeID1, routeID2}, {routeID1, routeID2} }
    private static List<List<Double>> routesDistanceOfBillboards;   // { {routeID1, routeID2}, {routeID1, routeID2} }
    private static List<List<Integer>> routeIndexesOfBillboards;   // { {routeIndex1, routeIndex2}, {routeIndex1, routeIndex2} }
    private List<Route> routes;  // use routeIndex1 to retrieve route object

    public FinalResultReader() {
        start = System.currentTimeMillis();
        routes = new ArrayList<>();
    }

    public void reload() {
        panelIDs = new ArrayList<>();
        weeklyImpressions = new ArrayList<>();
        routeIDSet = new TreeSet<>();
        lantitudeList = new ArrayList<>();
        longitudeList = new ArrayList<>();
        routeIDsOfBillboards = new ArrayList<>();   // { {routeID1, routeID2}, {routeID1, routeID2} }
        routesDistanceOfBillboards = new ArrayList<>();   // { {routeID1, routeID2}, {routeID1, routeID2} }
        routeIndexesOfBillboards = new ArrayList<>();   // { {routeIndex1, routeIndex2}, {routeIndex1, routeIndex2} }

        init();
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public List<Billboard> getBillboards() {

        newRoutes();
        ArrayList<Billboard> newBillboards = new ArrayList<>();

        for (int i = 0; i < panelIDs.size(); i++) {

            if (routeIndexesOfBillboards.get(i).size() == 0)
                continue;

            Billboard newBillboard = new Billboard();

            newBillboard.panelID = panelIDs.get(i);
            newBillboard.latitude = lantitudeList.get(i);
            newBillboard.longitude = longitudeList.get(i);
            //newBillboard.charge = weeklyImpressions.get(i) / 3000;

            List<Integer> routeIndexes = routeIndexesOfBillboards.get(i);

            for (int j = 0; j < routeIndexes.size(); j++) {
                int routeIndex = routeIndexes.get(j);
                Route route = routes.get(routeIndex);
                route.resetInf();
                newBillboard.distances.add(routesDistanceOfBillboards.get(i).get(j));
                newBillboard.addRoute(route);
            }
            newBillboard.influence = newBillboard.getRoutes().size();
            newBillboard.charge = Math.pow(newBillboard.influence,0.7);
            newBillboard.influencePerCharge = Math.ceil(newBillboard.influence / newBillboard.charge);
            newBillboard.orderRoute();
            newBillboards.add(newBillboard);
        }
        long end = System.currentTimeMillis();
        if (Setting.test)
            System.out.println("Time cost of Initialization : " + (end - start) / 1000.0 + " s");
        return newBillboards;
    }


    // initialize panelIDs, weeklyImpressions, routeIDSet, routeIDsOfBillboards, routeIndexesOfBillboards
    private void init() {

        if (Setting.getMultiBillboard() == 0)
            Setting.setMultiBillboard(1);

        System.out.println("Reading File");

        double infProb = Setting.infProb;
        Random random = new Random();
        MyFileReader finalResultReader = new MyFileReader();

        String line;

        int billboardID = 1;
        while (true) {

            if (Setting.test && Setting.getBillboardNumber() > 0 && billboardID > Setting.getBillboardNumber()) {
                System.out.println("For testing, only the first " + Setting.getBillboardNumber()
                        + " billboards will be loaded.");
                break;
            }

            line = finalResultReader.getNextLine();
            if (line == null)
                break;

            String[] elements;
            if (Setting.fileType == 2)
                elements = line.split(",\t");
            else
                elements = line.split(",");
            if (elements.length == 1)
                continue;    // skip those billboard which can not influence any route

            String panelID = String.valueOf(billboardID);
            int weeklyImpression = 1;

            Set<Integer> routeIDSet = new HashSet<>();
            List<Integer> routeIDs = new ArrayList<>();
            List<Double> distances = new ArrayList<>();

            if (elements.length > 2) {
                String[] routes = new String[0];
                String[] route;

                if (Setting.fileType == 1)
                    routes = elements[2].split("/");
                else if (Setting.fileType == 2)
                    routes = Arrays.copyOfRange(elements, 1, elements.length);

                int trajCount = 0;
                for (String s : routes) {
                    if (trajCount++ > (routes.length * Setting.getTrajPer()))
                        break;

                    if (random.nextDouble() >= infProb)
                        continue;

                    int routeID = 0;
                    double distance = 0;

                    if (Setting.fileType == 1) {
                        route = s.split(":");
                        routeID = Integer.parseInt(route[0].trim());
                        distance = Double.parseDouble(route[1].trim());
                    } else if (Setting.fileType == 2) {
                        routeID = Integer.parseInt(s);
                        distance = 1;
                    }

                    if (!routeIDSet.contains(routeID)) {
                        routeIDSet.add(routeID);
                        routeIDs.add(routeID);
                        distances.add(distance);
                    }
                }
            }

            if (routeIDs.size() == 0)
                continue;

            for (int m = 0; m < Setting.getMultiBillboard(); m++) {
                if (m == 0)
                    panelIDs.add(panelID);
                else
                    panelIDs.add(m + panelID);
                weeklyImpressions.add(weeklyImpression);
                routeIDsOfBillboards.add(routeIDs);
                routesDistanceOfBillboards.add(distances);

                lantitudeList.add(Double.valueOf(elements[0]));
                longitudeList.add(Double.valueOf(elements[1]));

            }

            billboardID++;
        }
        setUpRouteIDsAndIndexes();
        finalResultReader.close();
    }

    // set up routeIDSet and routeIndexesOfBillboards
    private void setUpRouteIDsAndIndexes() {

        if (Setting.test)
            System.out.println("Adding routes to routeIDSet");


        for (List<Integer> routeIDs : routeIDsOfBillboards) {
            routeIDSet.addAll(routeIDs);
        }


        List<Integer> routeIDSetList = new ArrayList<>(routeIDSet);

        if (Setting.test)
            System.out.println("Building route index for billboards, billboards "
                    + routeIDsOfBillboards.size() + " |  Route Size " + routeIDSet.size());

        for (int i = 0; i < routeIDsOfBillboards.size(); i++) {

            List<Integer> routeIndexes = new ArrayList<>();
            List<Integer> routeIDs = routeIDsOfBillboards.get(i);

            if (Setting.test && (i % 50 == 0))
                System.out.println("Finish " + i + " / " + routeIDsOfBillboards.size());

            for (int routeID : routeIDs) {
                int routeIndex = getRouteIndex(routeID, routeIDSetList);
                routeIndexes.add(routeIndex);
            }

            routeIndexesOfBillboards.add(routeIndexes);
        }
    }

    private int getRouteIndex(int routeID, List<Integer> routeIDs) {
        int start = 0;
        int end = routeIDs.size();
        int index, id;

        while (start != end) {
            index = (start + end) / 2;
            id = routeIDs.get(index);
            if (id > routeID)
                end = (start + end) / 2;
            else if (id < routeID)
                start = (start + end) / 2;
            else return index;
        }

        System.out.println("can not get index for routeID : " + routeID);
        return -1;
    }


    private void newRoutes() {

        List<Integer> routeIDs = new ArrayList<>(routeIDSet);

        for (int routeID : routeIDs) {

            Route route = new Route(routeID);
            route.resetInf();

            routes.add(route);
        }
    }

}

