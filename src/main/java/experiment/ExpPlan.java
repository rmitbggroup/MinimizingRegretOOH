package experiment;

import entity.ExpSetting;

import java.util.ArrayList;

public class ExpPlan {

    ArrayList<ExpSetting> expPlanList;
    ExpSetting defaultSetting = new ExpSetting();

    public ExpPlan() {
        expPlanList = new ArrayList<>();
        setDefaultSetting();
        //avg inf = 20% - 1%
        expPlanList.add(exp01());
        expPlanList.add(exp02());
        expPlanList.add(exp03());
        expPlanList.add(exp04());
        expPlanList.add(exp05());


    }

    private void setDefaultSetting() {
        defaultSetting.trajPer = 1;
        defaultSetting.multiBillboard = 1;
        defaultSetting.beta = .1;
        defaultSetting.gamma = .5;
        defaultSetting.infProb = 1;
        defaultSetting.lambda = 200;
        defaultSetting.model = 1;
        defaultSetting.range = "";
        defaultSetting.algs = new int[]{1, 2, 9, 7};
        defaultSetting.test = true;
        defaultSetting.randomBudget = true;
        defaultSetting.infPercentage = 1;
        defaultSetting.startPayment = 5;
    }

    public ArrayList<ExpSetting> getExpPlanList() {
        return expPlanList;
    }

    private ExpSetting exp01() {
        ExpSetting exp1 = new ExpSetting(defaultSetting);
        exp1.infPercentage = 1.2;
        exp1.startPayment = 5;
        return exp1;
    }

    private ExpSetting exp02() {
        ExpSetting exp1 = new ExpSetting(defaultSetting);
        exp1.infPercentage = 1;
        exp1.startPayment = 5;
        return exp1;
    }

    private ExpSetting exp03() {
        ExpSetting exp1 = new ExpSetting(defaultSetting);
        exp1.infPercentage = 0.8;
        exp1.startPayment = 5;
        return exp1;
    }

    private ExpSetting exp04() {
        ExpSetting exp1 = new ExpSetting(defaultSetting);
        exp1.infPercentage = .6;
        exp1.startPayment = 5;
        return exp1;
    }

    private ExpSetting exp05() {
        ExpSetting exp1 = new ExpSetting(defaultSetting);
        exp1.infPercentage = .4;
        exp1.startPayment = 5;
        return exp1;
    }
}
