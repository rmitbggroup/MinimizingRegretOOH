package entity;

import java.util.Arrays;

public class Route implements Comparable<Route> {

    private int routeID;
    private int advNum;
    private boolean influenced; // whether has been influenced by any billboard
    private Boolean[] infCheckList; // which advertisers have influenced this route
    private Integer[] infNumList; // how many time influence by which advertisers

    //private Integer infNum = 0; // this variable is only for the adv 9999 to evaluate inf

    public Route(int routeID) {
        this.routeID = routeID;
        advNum = Setting.getAdvNum();
        influenced = true;
        infCheckList = new Boolean[advNum];
        infNumList = new Integer[advNum];
        resetInf();
    }

    public void resetAdvNum() {
        advNum = Setting.getAdvNum();
        infCheckList = new Boolean[advNum];
        infNumList = new Integer[advNum];
        Arrays.fill(infCheckList, false);
        Arrays.fill(infNumList, 0);
    }

    //influenced by one advertiser
    public void infByBillboard(int advID) {
        influenced = true;
        infCheckList[advID] = true;
        infNumList[advID]++;
    }

    /**
     * undo influence
     *
     * @param advID undo influence for advID
     * @return return true if none of billboard of this advertiser could influence this route, then remove this route in the corresponding BillboardSet
     */
    public boolean unpickByBillboard(int advID) {
        if (infNumList[advID] <= 1) {
            infNumList[advID] = 0;
            infCheckList[advID] = false;
            return true;
        } else {
            infNumList[advID]--;
            return false;
        }
    }

    //reset this route for all advertisers
    public void resetInf() {
        if (influenced) {
            influenced = false;
            Arrays.fill(infCheckList, false);
            Arrays.fill(infNumList, 0);
        }
    }

    //reset this route for one advertiser
    public void resetInf(int advID) {
        infCheckList[advID] = false;
        infNumList[advID] = 0;
    }

    //calculate the lossing influence of losing one selected billboard
    public double getLossInf(int advID) {
        // one touch influence model
        if (infNumList[advID] > 1)
            return 0;
        else return 1;
    }

    //calculate marginal influence
    public double getMargInf(int advID) {
        if (advID >= 0) {
            if (infCheckList[advID])
                return 0.0;
            else
                return 1.0;
        } else {
            return 1;
        }
    }

    public double getInf(int advID) {
        if (infNumList[advID] > 0)
            return 1;
        else return 0;
    }

    public int getRouteID() {
        return routeID;
    }

    @Override
    public int compareTo(Route o) {
        return routeID - o.routeID;
    }

}
