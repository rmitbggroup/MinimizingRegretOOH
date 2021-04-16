package algorithms;

import entity.*;

import java.util.*;

public class GreedyFunction {
    private int index;
    private double gamma;

    public GreedyFunction() {
        this.gamma = Setting.gamma;
    }

    public Billboard greedyOne(List<Billboard> remainingBillboards, int advID) {
        //get one billboard with pre-order
        index = 0;
        order(remainingBillboards, advID);
        return nextOne(remainingBillboards);
    }

    // 0-1,    0 totally overlap,   1 no overlap
    private double getMaxOverlapRatio(BillboardSet adv, Billboard billboard) {
        double maxOverlapRatio = 0;
        double overlapRatio;
        for (Billboard targetBillboard : adv.getAllBillboards()) {
            overlapRatio = targetBillboard.getOverlapRatio(billboard);
            if (overlapRatio > maxOverlapRatio)
                maxOverlapRatio = overlapRatio;
        }
        return 1 - maxOverlapRatio;
    }

    public Billboard greedyRegret(List<Billboard> remainingBillboards, BillboardSet advBill, Advertiser advertiser) {
        double inf = advBill.getInfluence(false);
        double regret = getRegret(advertiser, inf);
        double optRegret = regret;
        double margInf;
        int advID = advertiser.getAdvID();
        index = -1;
        Billboard billboard;
        for (int i = 0; i < remainingBillboards.size(); i++) {
            billboard = remainingBillboards.get(i);
            billboard.updateMargInf(advID);
            margInf = billboard.getMargInf();
            regret = getRegret(advertiser, inf + margInf);
            if (regret <= optRegret) {
                optRegret = regret;
                index = i;
            }
        }
        if (index != -1)
            return remainingBillboards.get(index);
        else
            return null;
    }


    public Billboard greedyOneLazyFast(List<Billboard> remainingBillboards, BillboardSet adv, double requestedInf) {
        double optInf = 0;
        double inf, maxOverlapRatio;
        int advID = adv.getAdvID();
        index = 0;
        Billboard billboard;
        for (int i = 0; i < remainingBillboards.size(); i++) {
            billboard = remainingBillboards.get(i);
            maxOverlapRatio = getMaxOverlapRatio(adv, billboard);
            if (requestedInf > optInf) {
                if (billboard.maxInf() * maxOverlapRatio > optInf) {
                    billboard.updateMargInf(advID);
                    inf = billboard.getMargInf();
                    if (inf > optInf) {
                        optInf = inf;
                        index = i;
                    }
                } else {
                    return remainingBillboards.get(index);
                }
            } else {
                if (billboard.maxInf() * maxOverlapRatio > requestedInf) {
                    billboard.updateMargInf(advID);
                    inf = billboard.getMargInf();
                    if (inf < optInf && inf > requestedInf) {
                        optInf = inf;
                        index = i;
                    }
                } else {
                    return remainingBillboards.get(index);
                }
            }
        }
        if (remainingBillboards.size() > 0)
            return remainingBillboards.get(0);
        return null;
    }

    public Billboard nextOne(List<Billboard> remainingBillboards) {
        if (remainingBillboards.size() > index)
            return remainingBillboards.get(index++);
        else
            return null;
    }

    /**
     * order remaining billboards for a particular advertiser
     *
     * @param remainingBillboards remaining billboards
     * @param advID               advertiser ID
     */
    public void order(List<Billboard> remainingBillboards, int advID) {
        //update marginal influence of all remaining billboards
        for (Billboard billboard : remainingBillboards) {
            billboard.updateMargInf(advID);
        }

        //order by marginal influence/charge
        Collections.sort(remainingBillboards);
    }

    /**
     * order remaining billboards without considering any advertiser
     *
     * @param remainingBillboards remaining billboards
     */
    public void order(List<Billboard> remainingBillboards) {
        //update marginal influence of all remaining billboards
        for (Billboard billboard : remainingBillboards) {
            billboard.updateMargInf(-1);
        }

        //order by marginal influence/charge
        Collections.sort(remainingBillboards);
    }

    /**
     * order the advertiser's requests by the cost performance, so that we can serve high costPer request first.
     * costPer = payment / required influence
     */
    public void orderByCostPer(ArrayList<Advertiser> advertiserList) {
        int advNum = advertiserList.size();
        ArrayList<Double> costPer = new ArrayList<>();
        for (Advertiser advertiser : advertiserList) {
            costPer.add(advertiser.getBudget() / advertiser.getRequiredInf());
        }

        for (int i = 0; i < advNum - 1; i++) {
            for (int n = i + 1; n < advNum; n++) {
                if (costPer.get(i) < costPer.get(n)) {
                    Collections.swap(advertiserList, i, n);
                    Collections.swap(costPer, i, n);
                }
            }
        }
    }

    public void preselect(List<Billboard> remainingBoards, ArrayList<ArrayList<Billboard>> pickedBillboardLists,
                          BillboardSet[] resultSetList, int advNum) {
        // add pre-selected billboards for all advertisers
        int num = 0;
        for (int i = 0; i < advNum; i++) {
            for (int index = 0; index < pickedBillboardLists.get(i).size(); index++) {
                for (Billboard billboard : remainingBoards) {
                    if (pickedBillboardLists.get(i).get(index).panelID.equals(billboard.panelID)) {
                        resultSetList[i].add(billboard);
                        remainingBoards.remove(billboard);
                        num++;
                        break;
                    }
                }
            }
        }
        if (Setting.test)
            System.out.println("Total preselected billboards " + num);
    }

    public double[] getTotalRegretDetail(ArrayList<Advertiser> advertiserList, BillboardSet[] resultSetList) {
        double[] regretList = {0, 0, 0};
        double budget, inf, requestInf;

        int z = 0;
        for (int i = 0; i < advertiserList.size(); i++) {
            inf = resultSetList[i].getInfluence(true);
            budget = advertiserList.get(i).getBudget();
            requestInf = advertiserList.get(i).getRequiredInf();
            if (inf >= advertiserList.get(i).getRequiredInf()) {
                regretList[1] += budget * (inf - requestInf) / requestInf;
                regretList[2] += 0;
            } else {
                regretList[0] += budget * (1 - gamma * (inf / requestInf));
                z++;
            }
        }
        if (z == advertiserList.size())
            System.out.println("!!!");
        return regretList;
    }

    public double getTotalRegret(ArrayList<Advertiser> advertiserList, BillboardSet[] resultSetList) {
        double totalRegret = 0.0;
        for (int i = 0; i < advertiserList.size(); i++) {
            totalRegret += getRegret(advertiserList.get(i), resultSetList[i]);
        }
        return totalRegret;
    }

    public double getTotalRegretForce(ArrayList<Advertiser> advertiserList, BillboardSet[] resultSetList) {
        double totalRegret = 0.0;
        for (int i = 0; i < advertiserList.size(); i++) {
            totalRegret += getRegretForce(advertiserList.get(i), resultSetList[i]);
        }
        return totalRegret;
    }

    public double getRegret(Advertiser advertiser, double inf) {
        double requestInf = advertiser.getRequiredInf();
        double budget = advertiser.getBudget();
        double regret;
        if (inf >= advertiser.getRequiredInf()) {
            regret = budget * (inf - advertiser.getRequiredInf()) / requestInf;
        } else {
            regret = budget * (1 - gamma * (inf / requestInf));
        }
        return regret;
    }

    public double getRegretForce(Advertiser advertiser, BillboardSet resultSet) {
        double inf = resultSet.getInfluence(true);
        return getRegret(advertiser, inf);
    }

    public double getRegret(Advertiser advertiser, BillboardSet resultSet) {
        double inf = resultSet.getInfluence(false);
        return getRegret(advertiser, inf);
    }

    public int billboardOverLap(Billboard b1, Billboard b2) {
        Set<Route> routeSet = new HashSet<>();
        routeSet.addAll(b1.getRoutes());
        routeSet.addAll(b2.getRoutes());
        return b1.getRoutes().size() + b2.getRoutes().size() - routeSet.size();
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }
}
