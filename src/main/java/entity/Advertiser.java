package entity;

public class Advertiser {

    private int advID;
    private double requiredInf; // required influence
    private double budget; // payment

    public Advertiser(int advID, double requiredInf, double budget) {
        this.advID = advID;
        this.requiredInf = requiredInf;
        this.budget = budget;
    }

    public int getAdvID() {
        return advID;
    }

    public double getRequiredInf() {
        return requiredInf;
    }

    public double getBudget() {
        return budget;
    }
}
