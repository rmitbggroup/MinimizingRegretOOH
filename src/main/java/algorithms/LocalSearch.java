package algorithms;

import entity.*;

import java.util.*;

//billboard-driven local search
public class LocalSearch {

    private int advNum; // how many advertisers
    private double optInfGain = 0;
    public boolean isUpdate = false;

    private int billIndex1, billIndex2;

    private ArrayList<Advertiser> advertiserList;
    private BillboardSet[] resultSetList; //final result for all advertisers
    private List<Billboard> remainingBoards; // all remaining billboards
    private ArrayList<Double> requiredInfList; // the required influence for each advertiser

    private GreedyFunction greedyFunction;

    public LocalSearch(List<Billboard> billboards, BillboardSet[] resultSetList, ExpSetting exp) {
        advNum = exp.advertierList.size();
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
        double regret;
        double updateTimes = 0;
        boolean update1 = true, update2 = true, update3 = true, update4 = true;
        while (update1 || update2 || update3 || update4) {
            updateTimes++;
            update1 = false;
            update2 = false;
            update3 = false;
            update4 = false;

            update1 = exchangeBillboard();

            for (int i = 0; i < resultSetList.length - 1; i++) {
                BillboardSet targetAdv = resultSetList[i];
                if (releaseBillboard(targetAdv, advertiserList.get(i)))
                    update3 = true;
            }

            for (int i = 0; i < resultSetList.length - 1; i++) {
                BillboardSet targetAdv = resultSetList[i];
                if (exchangeUnused(targetAdv, advertiserList.get(i)))
                    update2 = true;
            }

            if (remainingBoards.size() > 0)
                update4 = checkUnusedBillboards();

        }
        if (updateTimes > 1)
            isUpdate = true;
    }


    private boolean exchangeBillboard() {
        boolean update = false;
        int advID1, advID2;
        do {
            advID1 = -1;
            advID2 = -1;
            BillboardSet targetAdv, searchAdv;

            for (int i = 0; i < resultSetList.length - 1; i++) {
                targetAdv = resultSetList[i];

                if (targetAdv.getInfluence(false) == 0)
                    continue;

                if (targetAdv.getInfluence(false) > advertiserList.get(i).getRequiredInf())
                    continue;

                //try to exchange billboards
                for (int n = i + 1; n < resultSetList.length; n++) {
                    searchAdv = resultSetList[n];

                    if (searchAdv.getInfluence(false) == 0)
                        continue;

                    if (checkExchange(targetAdv, searchAdv, advertiserList.get(i), advertiserList.get(n)
                    )) {
                        update = true;
                        advID1 = i;
                        advID2 = n;
                        break;
                    }
                }
                if (advID1 != advID2)
                    break;
            }

            if (advID1 != advID2) {
                Billboard billboard1 = resultSetList[advID1].getAllBillboards().get(billIndex1);
                Billboard billboard2 = resultSetList[advID2].getAllBillboards().get(billIndex2);
                resultSetList[advID1].remove(billIndex1);
                resultSetList[advID2].remove(billIndex2);
                resultSetList[advID1].add(billboard2);
                resultSetList[advID2].add(billboard1);

            }
        } while (advID1 != advID2);
        return update;
    }

    private boolean checkUnusedBillboards() {
        if (remainingBoards.size() == 0)
            return false;

        int num = remainingBoards.size();

        for (int i = 0; i < advNum; i++) {
            trySatisfyAdv(i);
        }

        return remainingBoards.size() != num;
    }

    private void trySatisfyAdv(int index) {
        while (advertiserList.get(index).getRequiredInf() > resultSetList[index].getInfluence(false)) {
            Billboard billboard = greedyFunction.greedyOneLazyFast(remainingBoards, resultSetList[index], requiredInfList.get(index));
            if (billboard == null)
                break;
            resultSetList[index].add(billboard);
            remainingBoards.remove(billboard);
        }
    }

    /**
     * Try to release one billboard from target advertiser
     *
     * @param targetSet BillboardSet of target advertiser
     * @param targetAdv target advertiser
     * @return return true if there is a changing or releasing, otherwise return false
     */
    private Boolean releaseBillboard(BillboardSet targetSet, Advertiser targetAdv) {
        boolean update = false;
        double optInfGain, extraInf;
        int index;

        extraInf = targetSet.getInfluence(false) - targetAdv.getRequiredInf();
        if (extraInf <= 0)
            return false;

        do {
            index = -1;
            optInfGain = 0;

            for (int i = 0; i < targetSet.getBillboardNumber(); i++) {
                if (targetSet.getBillboard(i).influence < optInfGain)
                    continue;
//                double lost = targetSet.infLose(i);
//                System.out.println("Bill " + i + " inf lost " + lost);
                if (extraInf >= targetSet.infLose(i)) {
                    if (optInfGain < targetSet.getBillboard(i).influence) {
                        optInfGain = targetSet.getBillboard(i).influence;
                        index = i;
                    }
                }
            }

            if (index != -1) {
                //System.out.println("Adv " + targetAdv.getAdvID() + " removing billboard " + index + " , inf gian " + optInfGain);
                remainingBoards.add(targetSet.getBillboard(index));
                targetSet.remove(index);
                extraInf = targetSet.getInfluence(false) - targetAdv.getRequiredInf();
                update = true;
            }
        } while (index != -1);

        return update;
    }

    /**
     * exchange one billboard with the unused billboard
     *
     * @return
     */
    private boolean exchangeUnused(BillboardSet targetSet, Advertiser targetAdv) {
        //Step 2. exchange with unused billboards
        boolean update = false;
        double optInfGain;

        if (remainingBoards.size() == 0 || targetSet.getAllBillboards().size() == 0)
            return update;

        double gain, lost;
        // current influence
        double targetInf;
        // requested influence
        double targetReqInf = targetAdv.getRequiredInf();

        double extraInf, overlapInf;

        double regret, newRegret = 0;

        Billboard b2;

        do {
            targetInf = targetSet.getInfluence(false);
            extraInf = targetInf - targetReqInf;

            if (extraInf <= 5)
                return update;

            optInfGain = 0;
            billIndex1 = -1;
            billIndex2 = -1;
            regret = greedyFunction.getRegret(targetAdv, targetInf);

            for (int i = 0; i < targetSet.getAllBillboards().size(); i++) {
                lost = targetSet.infLose(i);
                for (int j = 0; j < remainingBoards.size(); j++) {
                    b2 = remainingBoards.get(j);
                    if (extraInf - lost + b2.influence < 0)
                        continue;

                    gain = targetSet.infGain(b2);

                    if (lost <= gain)
                        continue;

                    overlapInf = greedyFunction.billboardOverLap(targetSet.getBillboard(i), b2);

                    if (extraInf - lost + gain - overlapInf < 0)
                        continue;

                    newRegret = greedyFunction.getRegret(targetAdv, targetInf - lost + gain + overlapInf);

                    if (optInfGain < (regret - newRegret)) {
                        optInfGain = regret - newRegret;
                        billIndex1 = i;
                        billIndex2 = j;
                        update = true;
                    }
                }
            }
            if (billIndex1 != -1) {
                Billboard billboard1 = targetSet.getAllBillboards().get(billIndex1);
                Billboard billboard2 = remainingBoards.get(billIndex2);
                targetSet.remove(billboard1);
                remainingBoards.remove(billIndex2);
                targetSet.add(billboard2);
                remainingBoards.add(billboard1);
            }
        } while (billIndex1 != -1);
        return update;
    }

    private boolean checkExchange(BillboardSet targetSet, BillboardSet searchSet, Advertiser targetAdv,
                                  Advertiser searchAdv) {
        boolean update = false;
        double infGain, gain1, gain2, lost1, lost2;
        double searchInf = searchSet.getInfluence(false);
        double searchReqInf = searchAdv.getRequiredInf();

        ArrayList<Billboard> billList1 = targetSet.getAllBillboards();
        ArrayList<Billboard> billList2 = searchSet.getAllBillboards();

        Billboard b1, b2;

        for (int i = 0; i < billList1.size(); i++) {
            b1 = billList1.get(i);
            lost1 = targetSet.infLose(i);
            gain2 = searchSet.infGain(b1);
            for (int j = 0; j < billList2.size(); j++) {
                b2 = billList2.get(j);

                lost2 = searchSet.infLose(j);
                if (searchReqInf > searchInf - lost2 + b1.influence)
                    continue;
                infGain = b1.influence - lost1 + b2.influence;
                if (infGain <= optInfGain)
                    continue;
                gain1 = targetSet.infGain(b2);
                if (searchReqInf > searchInf - lost2 + gain2)
                    continue;
                infGain = gain1 - lost1;
                if (infGain <= optInfGain)
                    continue;

                optInfGain = infGain;
                billIndex1 = i;
                billIndex2 = j;
                update = true;
            }
        }
        return update;
    }
}
