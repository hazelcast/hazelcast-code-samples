package net.wrmay.jetdemo;

import java.util.Random;

//TODO for performance reasons, implement CompactSerializers for all of the classes in this package

public class MachineProfile {
    private String serialNum;
    private String manufacturer;
    private int warningTemp;
    private int criticalTemp;
    private int maxRPM;

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getWarningTemp() {
        return warningTemp;
    }

    public void setWarningTemp(int warningTemp) {
        this.warningTemp = warningTemp;
    }

    public int getCriticalTemp() {
        return criticalTemp;
    }

    public void setCriticalTemp(int criticalTemp) {
        this.criticalTemp = criticalTemp;
    }

    public int getMaxRPM() {
        return maxRPM;
    }

    public void setMaxRPM(int maxRPM) {
        this.maxRPM = maxRPM;
    }

    /////// for generating fake data

    private static final Random random = new Random();
    private static final String[] companies = new String [] {"Breton","Fabplus", "Laguna Tools", "Snapmaker","Machinecraft", "Multicam", "OZ Machine"};

    private static final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String digits = "0123456789";

    private static final int [] rpmLimits = new int[] {8000,10000,12000,20000,30000,40000};

    private static final  int [] warningTemps = new int [] {100,150,210, 300};

    private static String randomSN(){
        char [] result = new char[6];
        for(int i=0; i < 3; ++i) result[i] = letters.charAt(random.nextInt(letters.length()));
        for(int j=3; j < result.length; ++j) result[j] = digits.charAt(random.nextInt(digits.length()));
        return new String(result);
    }

    private static String randomCompany(){
        return companies[random.nextInt(companies.length)];
    }
    private static int randomWarningTemp(){
        return warningTemps[random.nextInt(warningTemps.length)];
    }

    private static int randomMaxRPM(){
        return rpmLimits[random.nextInt(rpmLimits.length)];
    }

    // not thread safe
    public static MachineProfile fake(){
        MachineProfile result = new MachineProfile();

        result.setManufacturer(randomCompany());
        result.setSerialNum(randomSN());
        result.setWarningTemp(randomWarningTemp());
        result.setCriticalTemp(result.warningTemp + 30);
        result.setMaxRPM(randomMaxRPM());

        return result;
    }
    public static MachineProfile special(){
        MachineProfile result = new MachineProfile();

        result.setManufacturer(randomCompany());
        result.setSerialNum(Names.SPECIAL_SN);
        result.setWarningTemp(200);
        result.setCriticalTemp(240);
        result.setMaxRPM(randomMaxRPM());

        return result;
    }

}
