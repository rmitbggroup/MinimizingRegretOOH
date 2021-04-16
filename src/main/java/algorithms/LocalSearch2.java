package algorithms;

import entity.*;

import java.util.ArrayList;
import java.util.List;

//advertiser-driven local search
public class LocalSearch2 {

    private int advNum; // how many advertisers
    private double optRegret;
    public boolean isUpdate = true;

    private ExpSetting expSetting;
    private ArrayList<Advertiser> advertiserList;
    private BillboardSet[] resultSetList; //final result for all advertisers
    private List<Billboard> remainingBoards; // all remaining billboards
    private ArrayList<Double> requiredInfList; // the required influence for each advertiser

    private GreedyFunction greedyFunction;

    public LocalSearch2(List<Billboard> billboards, BillboardSet[] resultSetList, ExpSetting exp) {
        advNum = exp.advertierList.size();
        this.expSetting = exp;
        this.remainingBoards = billboards;
        this.requiredInfList = new ArrayList<>();
        this.resultSetList = resultSetList;
        this.advertiserList = exp.advertierList;

        greedyFunction = new GreedyFunction();

        for (int i = 0; i < advNum; i++) {
            requiredInfList.add(exp.advertierList.get(i).getRequiredInf());
        }
    }

    public void getResult() {
        search();
    }

    private void search() {
        boolean update = false;
        int adv1, adv2;
        double optRegret = 0, regret1, regret2, newRegret, currentRegret;
        boolean[] satisfyList = new boolean[advNum];
        for (int i = 0; i < advNum; i++) {
            if (requiredInfList.get(i) <= resultSetList[i].getInfluence(false)) {
                satisfyList[i] = true;
            } else {
                satisfyList[i] = false;
                update = true;
            }
        }
        //there is no advertiser can be exchanged
        if (!update) {
            isUpdate = false;
            return;
        }
        do {
            update = false;
            for (int i = 0; i < advNum - 1; i++) {
                adv1 = adv2 = 0;
                regret1 = greedyFunction.getRegret(advertiserList.get(i), resultSetList[i]);
                for (int n = i + 1; n < advNum; n++) {
                    regret2 = greedyFunction.getRegret(advertiserList.get(n), resultSetList[n]);
                    currentRegret = regret1 + regret2;
                    newRegret = greedyFunction.getRegret(advertiserList.get(i), resultSetList[n]) + greedyFunction.getRegret(advertiserList.get(n), resultSetList[i]);
                    if (newRegret < currentRegret) {
                        if (optRegret < (currentRegret - newRegret)) {
                            update = true;
                            optRegret = currentRegret - newRegret;
                            adv1 = i;
                            adv2 = n;
                        }
                    }

                }
                if (adv1 != adv2) {
                    ArrayList<Billboard> billboardList1 = resultSetList[adv1].getAllBillboards();
                    ArrayList<Billboard> billboardList2 = resultSetList[adv2].getAllBillboards();
                    resultSetList[adv1].release();
                    resultSetList[adv2].release();
                    resultSetList[adv1].add(billboardList2);
                    resultSetList[adv2].add(billboardList1);
                }
            }

        } while (update);
    }
}
