package entity;

import java.util.ArrayList;

public class ExpSetting {

    public ExpSetting(ExpSetting defaultSetting) {
        this.timeCostList = new ArrayList<>();
        this.advertierList = new ArrayList<>();
        this.trajPer = defaultSetting.trajPer;
        this.multiBillboard = defaultSetting.multiBillboard;
        this.infPercentage = defaultSetting.infPercentage;
        this.beta = defaultSetting.beta;
        this.infProb = defaultSetting.infProb;
        this.lambda = defaultSetting.lambda;
        this.model = defaultSetting.model;
        this.range = defaultSetting.range;
        this.algs = defaultSetting.algs;
        this.algNames = new String[this.algs.length];
        this.test = defaultSetting.test;
        this.startPayment = defaultSetting.startPayment;
        this.randomBudget = defaultSetting.randomBudget;
        this.maxAdvNum = this.advNum;
        this.gamma = defaultSetting.gamma;
    }

    public ExpSetting() {
        this.timeCostList = new ArrayList<>();
        this.advertierList = new ArrayList<>();
        this.maxAdvNum = this.advNum;
    }

    //the total influence of all advertiser / the total influence of all billboards
    public double infPercentage;

    // the experiment ID is assigned when adding expSetting into list in Experiment Class.
    public int expIndex;

    public double infProb;

    // advertiser list consists of payment and required inf
    public ArrayList<Advertiser> advertierList;

    //lambda: Only used for data preprocessing to determine whether the trajectory is affected by a billboard
    public double lambda;

    //influence model: 1 submodular, 2 sigmoid
    public int model;

    //the number of advertisers
    public int advNum;

    //unsatisfied penalty
    public double gamma;

    //the final number of advertisers, advNum may be changed according to the needs (i.e., PKG)
    public int maxAdvNum;

    //test model
    public boolean test;

    // the limited number of billboards, only works in test model
    public int billboardNumber;

    public int routeNunber;

    //range: longitude1 / latitude1 / longitude2 / latitude2 / next range
    public String range;

    // multiply Billboard
    public int multiBillboard;

    // the percentage of trajectories that are used
    public double trajPer;

    //sigmoid
    //alpha&beta: Two parameters to control the shape of S-curve
    public double alpha;
    public double beta;

    //picked billboard
    public ArrayList<ArrayList<Billboard>> pickedBillboard;

    //algorithms
    //0 greedyForAll; 1 greedyForone
    public int[] algs;

    //algorithm name
    public String[] algNames;


    // for saving final result
    public BillboardSet[] resultSetList; //final result for all advertisers
    public double startPayment; // start payment
    public double totalPayment; // final payment
    public ArrayList<Long> timeCostList;
    public boolean randomBudget;
}
