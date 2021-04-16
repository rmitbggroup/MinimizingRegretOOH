package algorithms;

import entity.*;
import experiment.Initialization;

import java.util.*;

public class GRASP {

    private boolean LS = false; //local search1
    private boolean LS2 = false; //local search2
    private int RCLSIZE;
    private int maxIteration = 100;

    private int greedAlg = 0; //0 greedy+ ; 1 greedyOne

    private int advNum; // how many advertisers
    private double optTotalRegret = 999999999;
    public boolean countTime = true;

    private ExpSetting expSetting;
    private ArrayList<Advertiser> advertiserList;
    private BillboardSet[] resultSetList; //the result for all advertisers in each iteration
    private BillboardSet[] optResultSetList; //the final optimal result for all advertisers
    private List<Billboard> remainingBoards; // all remaining billboards
    private ArrayList<Billboard> rcl; // RCL

    private GreedyFunction greedyFunction;
    private Initialization initialization;

    public GRASP(List<Billboard> billboards, ExpSetting exp, Initialization initialization) {
        this.advNum = exp.advNum;
        this.RCLSIZE = advNum;
        this.resultSetList = new BillboardSet[advNum];
        this.optResultSetList = new BillboardSet[advNum];
        this.expSetting = exp;
        this.remainingBoards = billboards;
        this.initialization = initialization;
        this.advertiserList = exp.advertierList;
        // the required influence for each advertiser

        this.greedyFunction = new GreedyFunction();
        this.greedyFunction.orderByCostPer(this.advertiserList);

        for (int advID = 0; advID < advNum; advID++) {
            this.resultSetList[advID] = new BillboardSet(advID);
            this.optResultSetList[advID] = new BillboardSet(advID);
        }

        // add preselected billboards for each advertiser
        if (exp.pickedBillboard != null)
            this.greedyFunction.preselect(remainingBoards, exp.pickedBillboard, resultSetList, advNum);
    }

    public void findResult() {

        long start = System.currentTimeMillis();
        int unUpdateCount = 0; // the number of iteration that hasn't update
        Setting.greedyTimer = false;

        BillboardSet[] resultSet = constructRandomSolution(); // construct the first solution
        update(resultSet);

        LocalSearch localSearch;
        LocalSearch2 localSearch2;

        if (LS) {
            localSearch = new LocalSearch(remainingBoards, resultSet, expSetting);
            localSearch.getResult();
            if (localSearch.isUpdate)
                update(resultSet);
        }

        if (LS2) {
            localSearch2 = new LocalSearch2(remainingBoards, resultSet, expSetting);
            localSearch2.getResult();
            if (localSearch2.isUpdate)
                update(resultSet);
        }

        while (unUpdateCount < maxIteration) {
            unUpdateCount++;

            reset();
            resultSet = greedyRandomizedConstruction();
            //checkDoubleSelect(resultSet);

            if (LS2) {
                localSearch2 = new LocalSearch2(remainingBoards, resultSet, expSetting);
                localSearch2.getResult();
            }

            update(resultSet);//unUpdateCount = 0;

            if (LS) {
                localSearch = new LocalSearch(remainingBoards, resultSet, expSetting);
                localSearch.getResult();
            }

            update(resultSet);//unUpdateCount = 0;
        }

        long end = System.currentTimeMillis();

        System.out.println("GRASP finished");
        expSetting.resultSetList = optResultSetList;
        System.out.println("Current EXP regret " + greedyFunction.getTotalRegretForce(advertiserList, expSetting.resultSetList));

        if (countTime) {
            expSetting.timeCostList.add(end - start);
            System.out.println("Time cost " + (end - start) / 1000.0);
        }
    }

    private void reset() {
        remainingBoards = initialization.getBillboards();

        for (int i = 0; i < advNum; i++) {
            resultSetList[i] = new BillboardSet(i);
        }

        // add preselected billboards for each advertiser
        if (expSetting.pickedBillboard != null)
            greedyFunction.preselect(remainingBoards, expSetting.pickedBillboard, resultSetList, advNum);
    }

    private BillboardSet[] greedyRandomizedConstruction() {
        makeRCL();
        selectElementAtRandom();
        return adaptGreedyFunction();
    }

    private BillboardSet[] adaptGreedyFunction() {
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(remainingBoards, resultSetList, expSetting);
        greedyAlgorithm.countTime = false;
        if (greedAlg == 0)
            greedyAlgorithm.greedyForAllPlus();
        else
            greedyAlgorithm.greedyForOne();
        return greedyAlgorithm.getResultSetList();
    }

    private void selectElementAtRandom() {
        for (int i = 0; i < advNum; i++) {
            pickOne(rcl.get(i), i);
        }
    }

    private void makeRCL() {
        if (rcl == null || rcl.size() != RCLSIZE) {
            rcl = new ArrayList<>();
            rcl.addAll(remainingBoards.subList(0, RCLSIZE));
        }
        Collections.shuffle(rcl);
    }

    private BillboardSet[] constructRandomSolution() {
        //greedAlg
        BillboardSet[] billboardSets1, billboardSets2;
        GreedyAlgorithm greedyAlgorithm = new GreedyAlgorithm(remainingBoards, expSetting);
        greedyAlgorithm.countTime = false;

        greedyAlgorithm.greedyForAllPlus();
        billboardSets1 = greedyAlgorithm.getResultSetList();
        double regret1 = greedyFunction.getTotalRegret(advertiserList, billboardSets1);

        reset();
        greedyAlgorithm = new GreedyAlgorithm(remainingBoards, expSetting);
        greedyAlgorithm.greedyForOne();
        billboardSets2 = greedyAlgorithm.getResultSetList();
        double regret2 = greedyFunction.getTotalRegret(advertiserList, billboardSets2);

        if (regret1 <= regret2)
            return billboardSets1;

        System.out.println("Change to GreedyForOne");
        greedAlg = 1;
        return billboardSets2;
    }

    private boolean update(BillboardSet[] resultSet) {
        double regret = greedyFunction.getTotalRegretForce(advertiserList, resultSet);
        if (regret < optTotalRegret) {
            optTotalRegret = regret;
            optResultSetList = new BillboardSet[advNum];
            for (int i = 0; i < resultSet.length; i++) {
                BillboardSet billboardSet = new BillboardSet(resultSet[i]);
                optResultSetList[i] = billboardSet;
            }
            return true;
        }
        return false;
    }

    private void pickOne(Billboard billboard, int advID) {
        resultSetList[advID].add(billboard);
        remainingBoards.remove(billboard);
    }

    public void setLS(boolean LS) {
        this.LS = LS;
    }

    public void setLS2(boolean LS2) {
        this.LS2 = LS2;
    }
}
