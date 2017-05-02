package MyApp;

/**
 * @author "Sergey Edunov"
 * @version 1/13/11
 */
public class GrangerTestResult {

    private double fStat;
    private double r2;
    private double pValue;

    public GrangerTestResult(double fStat, double r2, double pValue) {
        this.fStat = fStat;
        this.r2 = r2;
        this.pValue = pValue;
    }

    public double getFStat() {
        return fStat;
    }

    public double getR2() {
        return r2;
    }

    public double getPValue() {
        return pValue;
    }
}
