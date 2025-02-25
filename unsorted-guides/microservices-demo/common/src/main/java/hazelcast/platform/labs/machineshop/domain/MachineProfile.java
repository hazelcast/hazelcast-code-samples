package hazelcast.platform.labs.machineshop.domain;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

import java.util.Random;


public class MachineProfile {
    private String serialNum;
    private String location;

    private String block;
    private float faultyOdds;
    private String manufacturer;
    private short warningTemp;
    private short criticalTemp;
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

    public short getWarningTemp() {
        return warningTemp;
    }

    public void setWarningTemp(short warningTemp) {
        this.warningTemp = warningTemp;
    }

    public short getCriticalTemp() {
        return criticalTemp;
    }

    public void setCriticalTemp(short criticalTemp) {
        this.criticalTemp = criticalTemp;
    }

    public int getMaxRPM() {
        return maxRPM;
    }

    public void setMaxRPM(int maxRPM) {
        this.maxRPM = maxRPM;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public float getFaultyOdds() {
        return faultyOdds;
    }

    public void setFaultyOdds(float faultyOdds) {
        this.faultyOdds = faultyOdds;
    }


    /////// for generating fake data

    private static final Random random = new Random();
    private static final String[] companies = new String [] {"Cyberdyne","Fabric8", "Catalina Tools", "Lex Corp","CNC Tool Works", "Omni Corp", "General Machine Tools"};

    private static final String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String digits = "0123456789";

    private static final int [] rpmLimits = new int[] {8000,10000,12000,20000,30000,40000};

    private static final short[] warningTemps = new short[] {100,150,210, 240};

    private static String randomSN(){
        char [] result = new char[6];
        for(int i=0; i < 3; ++i) result[i] = letters.charAt(random.nextInt(letters.length()));
        for(int j=3; j < result.length; ++j) result[j] = digits.charAt(random.nextInt(digits.length()));
        return new String(result);
    }

    private static String randomCompany(){
        return companies[random.nextInt(companies.length)];
    }
    private static short randomWarningTemp(){
        return warningTemps[random.nextInt(warningTemps.length)];
    }

    private static int randomMaxRPM(){
        return rpmLimits[random.nextInt(rpmLimits.length)];
    }

    // not thread safe
    public static MachineProfile fake(String location, String block, float pFaulty){
        MachineProfile result = new MachineProfile();

        result.setManufacturer(randomCompany());
        result.setSerialNum(randomSN());
        result.setLocation(location);
        result.setBlock(block);
        result.setFaultyOdds(pFaulty);
        result.setWarningTemp(randomWarningTemp());
        result.setCriticalTemp((short) (result.warningTemp + 60));
        result.setMaxRPM(randomMaxRPM());

        return result;
    }

    public static class Serializer implements CompactSerializer<MachineProfile> {

        @Override
        public MachineProfile read(CompactReader compactReader) {
            MachineProfile result = new MachineProfile();

            result.setSerialNum(compactReader.readString("serialNum"));
            result.setLocation(compactReader.readString("location"));
            result.setBlock(compactReader.readString("block"));
            result.setFaultyOdds(compactReader.readFloat32("faultyOdds"));
            result.setManufacturer(compactReader.readString("manufacturer"));
            result.setWarningTemp(compactReader.readInt16("warningTemp"));
            result.setCriticalTemp(compactReader.readInt16("criticalTemp"));
            result.setMaxRPM(compactReader.readInt32("maxRPM"));

            return result;
        }

        @Override
        public void write(CompactWriter compactWriter, MachineProfile machineProfile) {
            compactWriter.writeString("serialNum", machineProfile.getSerialNum());
            compactWriter.writeString("location", machineProfile.getLocation());
            compactWriter.writeString("block", machineProfile.getBlock());
            compactWriter.writeFloat32("faultyOdds", machineProfile.getFaultyOdds());
            compactWriter.writeString("manufacturer", machineProfile.getManufacturer());
            compactWriter.writeInt16("warningTemp", machineProfile.getWarningTemp());
            compactWriter.writeInt16("criticalTemp", machineProfile.getCriticalTemp());
            compactWriter.writeInt32("maxRPM", machineProfile.getMaxRPM());
        }

        @Override
        public String getTypeName() {
            return MachineProfile.class.getName();
        }

        @Override
        public Class<MachineProfile> getCompactClass() {
            return MachineProfile.class;
        }
    }
}
