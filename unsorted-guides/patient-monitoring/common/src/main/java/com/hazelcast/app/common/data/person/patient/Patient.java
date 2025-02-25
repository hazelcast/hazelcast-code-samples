package com.hazelcast.app.common.data.person.patient;

import com.hazelcast.app.common.data.person.profile.Profile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;

public class Patient extends Profile {
    private static final Logger LOGGER = LogManager.getLogger("Patient");

//    private static final long serialVersionUID = 3386724407622870440L;

    private String patientId;
    private String breathing;
    private String consciousness;
    private int heart;
    private int oxygenSaturationOne;
    private int oxygenSaturationTwo;
    private int respiratory;
    private int systolicBloodPressure;
    private float temperature;

    public Patient() {
    }

    public Patient(String patientIdIn) {
        super(patientIdIn);
        try {
            setPatientId(patientIdIn);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    public Patient(
            String patientIdIn
            , String breathingIn
            , String consciousnessIn
            , int heartIn
            , int oxygenSaturationOneIn
            , int oxygenSaturationTwoIn
            , int respiratoryIn
            , int systolicBloodPressureIn
            , float temperatureIn
    ) {
        super(patientIdIn);
        try {
            setPatientId(patientIdIn);
            setBreathing(breathingIn);
            setConsciousness(consciousnessIn);
            setHeart(heartIn);
            setOxygenSaturationOne(oxygenSaturationOneIn);
            setOxygenSaturationTwo(oxygenSaturationTwoIn);
            setRespiratory(respiratoryIn);
            setSystolicBloodPressure(systolicBloodPressureIn);
            setTemperature(temperatureIn);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientIdIn) {
        patientId = patientIdIn;
    }

    public String getBreathing() {
        return breathing;
    }

    public void setBreathing(String breathingIn) {
        breathing = breathingIn;
    }

    public String getConsciousness() {
        return consciousness;
    }

    public void setConsciousness(String consciousnessIn) {
        consciousness = consciousnessIn;
    }

    public int getHeart() {
        return heart;
    }

    public void setHeart(int heartIn) {
        heart = heartIn;
    }

    public int getOxygenSaturationOne() {
        return oxygenSaturationOne;
    }

    public void setOxygenSaturationOne(int oxygenSaturationOneIn) {
        oxygenSaturationOne = oxygenSaturationOneIn;
    }

    public int getOxygenSaturationTwo() {
        return oxygenSaturationTwo;
    }

    public void setOxygenSaturationTwo(int oxygenSaturationTwoIn) {
        oxygenSaturationTwo = oxygenSaturationTwoIn;
    }

    public int getRespiratory() {
        return respiratory;
    }

    public void setRespiratory(int respiratoryIn) {
        respiratory = respiratoryIn;
    }

    public int getSystolicBloodPressure() {
        return systolicBloodPressure;
    }

    public void setSystolicBloodPressure(int systolicBloodPressureIn) {
        systolicBloodPressure = systolicBloodPressureIn;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperatureIn) {
        temperature = temperatureIn;
    }

    public String getFullProfile() {
        return "Rm: " + getLocation() + " - " +
                getFirstName() + " " +
                getLastName() + " (" +
                getGender() + ")";
    }

}