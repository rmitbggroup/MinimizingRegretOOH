package algorithms;

import java.util.*;

import entity.*;

public class GreedyAlgorithm {

    private int advNum; // how many advertisers
    private boolean ifDisplayAlgName;

    public boolean countTime = true;

    private ExpSetting expSetting;
    private ArrayList<Advertiser> advertiserList;
    private BillboardSet[] resultSetList; //final result for all advertisers
    private List<Billboard> remainingBoards; // all remaining billboards
    private Double[] requiredInfList; // the required influence for each advertiser
    private Boolean[] isSatisfyList;

    private GreedyFunction greedyFunction;

    public GreedyAlgorithm(List<Billboard> billboards, BillboardSet[] resultSetList, ExpSetting exp) {
        ifDisplayAlgName = false;
        this.expSetting = exp;
        this.remainingBoards = billboards;
        this.advertiserList = exp.advertierList;
        this.advNum = advertiserList.size();
        this.resultSetList = resultSetList;
        this.requiredInfList = new Double[advNum];
        this.isSatisfyList = new Boolean[advNum];

        this.greedyFunction = new GreedyFunction();
        //order according to the costPer
        this.greedyFunction.orderByCostPer(advertiserList);

        for (int i = 0; i < advNum; i++) {
            this.resultSetList[i].setAdvID(exp.advertierList.get(i).getAdvID());
            this.requiredInfList[i] = exp.advertierList.get(i).getRequiredInf();
            this.isSatisfyList[i] = false;
        }
    }


    public GreedyAlgorithm(List<Billboard> billboards, ExpSetting exp) {
        ifDisplayAlgName = true;
        this.advNum = exp.advertierList.size();
        this.resultSetList = new BillboardSet[advNum];
        this.requiredInfList = new Double[advNum];
        this.expSetting = exp;
        this.remainingBoards = billboards;
        this.advertiserList = exp.advertierList;
        this.isSatisfyList = new Boolean[advNum];

        this.greedyFunction = new GreedyFunction();
        //order according to the costPer
        this.greedyFunction.orderByCostPer(advertiserList);

        double inf = 0;
        for (int i = 0; i < exp.advertierList.size(); i++) {
            this.resultSetList[i] = new BillboardSet(exp.advertierList.get(i).getAdvID());
            this.requiredInfList[i] = exp.advertierList.get(i).getRequiredInf();
            inf += this.requiredInfList[i];
            this.isSatisfyList[i] = false;
        }

        // add preselected billboards for each advertiser
        if (exp.pickedBillboard != null)
            this.greedyFunction.preselect(remainingBoards, exp.pickedBillboard, resultSetList, advNum);
    }

    /**
     * Algorithm 0:
     * In each iteration, for each advertiser, selecting one billboard that
     * could maximize the marginal influence
     */
    public void greedyForAll() {
        long start = System.currentTimeMillis();
        Billboard billboard;

        // find result greedily
        boolean isFinish = false;
        while (remainingBoards.size() > 0 && !isFinish) {
            isFinish = true;
            for (int advID = 0; advID < advNum; advID++) {
                if (resultSetList[advID].getInfluence(true) < requiredInfList[advID]) {
                    billboard = greedyFunction.greedyOne(remainingBoards, advID);
                    if (billboard != null) {
                        pickOne(billboard, advID);
                        isFinish = false;
                    }
                }

                if (remainingBoards.size() == 0)
                    break;
            }
        }
        expSetting.resultSetList = resultSetList;
        long end = System.currentTimeMillis();
        if (countTime && ifDisplayAlgName)
            expSetting.timeCostList.add(end - start);
    }

    /**
     * Algorithm 1:
     * In each iteration, select billboards that could maximize the marginal influence
     * for one advertiser until out of budget
     */
    public void greedyForOne() {
        long start = System.currentTimeMillis();
        Billboard billboard;
        GreedyFunction greedyFunction = new GreedyFunction();

        // find result greedily
        for (int advID = 0; advID < advNum; advID++) {
            while ((resultSetList[advID].getInfluence(false) < requiredInfList[advID])
                    && (remainingBoards.size() > 0)) {
                //billboard = greedyFunction.greedyOneLazy(remainingBoards, advID);
                billboard = greedyFunction.greedyRegret(remainingBoards, resultSetList[advID], expSetting.advertierList.get(advID));
                pickOne(billboard, advID);
            }
        }
        expSetting.resultSetList = resultSetList;
        long end = System.currentTimeMillis();
        if (Setting.greedyTimer)
            expSetting.timeCostList.add(end - start);
    }

    /**
     * Algorithm 2:
     * In each iteration, for each advertiser, selecting one billboard that
     * could maximize the marginal influence.
     * If all billboards have been chosen, then release selected billboards that have been chosen
     * from the advertiser who has the lowest cost performance
     */
    public void greedyForAllPlus() {
        long start = 0;
        start = System.currentTimeMillis();

        int releaseNum = 0; // how many advertisers have been released.
        Billboard billboard;

        int currentAdvNumSa;

        // find result greedily
        boolean isFinish = false;
        while (remainingBoards.size() > 0 && !isFinish) {
            isFinish = true;
            currentAdvNumSa = 0;

            for (int i = 0; i < advertiserList.size() - releaseNum; i++) {
                if (isSatisfyList[i] || resultSetList[i].jump)
                    continue;
                int advID = resultSetList[i].getAdvID();

                checkSatisfied(i);
                if (isSatisfyList[i]) {
                    continue;
                }

                //find billboard for this adv if we still have billboards.
                if (remainingBoards.size() > 0) {
                    billboard = greedyFunction.greedyRegret(remainingBoards, resultSetList[i], expSetting.advertierList.get(i));
                    if (billboard != null) {
                        pickOne(billboard, i);
                        isFinish = false;
                    }
                }

                //release billboards if more than one adv having been satisfied.
                if (remainingBoards.size() == 0) {
                    checkSatisfied(i);

                    for (int n = 0; n < advNum - 1 - releaseNum; n++) {
                        if (isSatisfyList[n])
                            currentAdvNumSa++;
                    }

                    if (advNum - currentAdvNumSa - releaseNum <= 1) {
                        isFinish = true;
                        break;
                    } else {
                        relaseAdv(advNum - 1 - releaseNum++);
                    }
                }
            }
        }

        if (remainingBoards.size() > 0) {
            for (int i = 0; i < advertiserList.size(); i++) {
                if (isSatisfyList[i]) {
                    continue;
                } else {
                    checkSatisfied(i);
                    if (isSatisfyList[i]) {
                        continue;
                    }
                    int advID = resultSetList[i].getAdvID();
                    while (remainingBoards.size() > 0 && !isSatisfyList[i]) {
                        //billboard = greedyFunction.greedyOneLazy(remainingBoards, advID);
                        billboard = greedyFunction.greedyRegret(remainingBoards, resultSetList[i], expSetting.advertierList.get(i));
                        pickOne(billboard, i);
                        checkSatisfied(i);
                    }

                }
            }
        }

        expSetting.resultSetList = resultSetList;
        if (Setting.greedyTimer) {
            long end = System.currentTimeMillis();
            expSetting.timeCostList.add(end - start);
        }
    }

    private void checkSatisfied(int advIndex) {
        double requiredInf = requiredInfList[advIndex] - resultSetList[advIndex].getInfluence(false);
        isSatisfyList[advIndex] = requiredInf <= 0;
    }

    /**
     * pick one billboard, move it from remainingBoards to pickedBillboards, and mark it as advID
     *
     * @param billboard the object of billboard
     * @param i         advertiser ID
     */
    private void pickOne(Billboard billboard, int i) {
        resultSetList[i].add(billboard);
        remainingBoards.remove(billboard);
        //billboard.pick(resultSetList[i].getAdvID());
    }

    private void relaseAdv(int i) {
        remainingBoards.addAll(resultSetList[i].getAllBillboards());
        resultSetList[i].release();
    }

    public BillboardSet[] getResultSetList() {
        return resultSetList;
    }
}