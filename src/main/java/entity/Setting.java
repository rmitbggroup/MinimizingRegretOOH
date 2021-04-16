package entity;

public class Setting {

    //1. NYC,   2. Singapore
    public static int fileType = 1;

    //0 linux; 1 window;
    public static int system = 1;

    // test model will display more details when running experiments
    public static boolean test = true;

    public static boolean greedyTimer = true;

    public static double gamma;

    // the probability of a trajectory of being influenced by a billboard
    public static double infProb = 1;

    //remove the last % of billboard
    public static double removeThreshold = 0;

    public static double beta = 0.1;

    // multiply Billboard
    private static int multiBillboard = 1;

    // the percentage of trajectories that are used
    private static double trajPer = 1;

    // the limited number of billboards, only works in test model
    private static int billboardNumber;

    //the number of advertisers;
    private static int advNum;

    // setter and getter

    public static int getBillboardNumber() {
        return billboardNumber;
    }

    public static void setBillboardNumber(int billboardNumber) {
        Setting.billboardNumber = billboardNumber;
    }

    public static int getAdvNum() {
        return advNum;
    }

    public static void setAdvNum(int advNum) {
        Setting.advNum = advNum;
    }

    public static void setMultiBillboard(int multiBillboard) {
        Setting.multiBillboard = multiBillboard;
    }

    public static void setTrajPer(double trajPer) {
        Setting.trajPer = trajPer;
    }

    public static int getMultiBillboard() {
        return multiBillboard;
    }

    public static double getTrajPer() {
        return trajPer;
    }
}
