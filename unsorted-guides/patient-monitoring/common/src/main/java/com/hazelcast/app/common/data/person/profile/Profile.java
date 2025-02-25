package com.hazelcast.app.common.data.person.profile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ThreadLocalRandom;

public class Profile implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger("Profile");

    private static final long serialVersionUID = 3386724407622870440L;

    private String profileId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private long birthDate;
    private String location;
    private boolean isActive;
    private long createdDttm;

    public Profile() {
    }

    public Profile(
            String profileIdIn
            , String firstNameIn
            , String middleNameIn
            , String lastNameIn
            , String genderIn
            , LocalDateTime birthDateIn
            , String locationIn
            , Boolean isActiveIn
            , LocalDateTime createdDttmIn
    ) {
        try {
            setProfileId(profileIdIn);
            setFirstName(firstNameIn);
            setMiddleName(middleNameIn);
            setLastName(lastNameIn);
            setGender(genderIn);
            setBirthDate(birthDateIn);
            setLocation(locationIn);
            setIsActive(isActiveIn);
            setCreatedDttm(createdDttmIn);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    public Profile(String profileIdIn) {
        try {
            Profile profile = ProfileUtils.getInstance().getProfile(profileIdIn);
            setProfileId(profile.getProfileId());
            setFirstName(profile.getFirstName());
            setMiddleName(profile.getMiddleName());
            setLastName(profile.getLastName());
            setGender(profile.getGender());
            setBirthDate(LocalDateTime.ofEpochSecond(profile.getBirthDate(), 0, ZoneOffset.UTC));
            setLocation(profile.getLocation());
            setIsActive(Boolean.parseBoolean(String.valueOf(profile.isActive())));
            setCreatedDttm(LocalDateTime.ofEpochSecond(profile.getCreatedDttm(), 0, ZoneOffset.UTC));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileIdIn) {
        profileId = profileIdIn;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstNameIn) {
        firstName = firstNameIn;
    }

    public String getMiddleName() {
        if (middleName == null) {
            middleName = String.valueOf((char) (ThreadLocalRandom.current().nextInt(26) + 'A'));
        }
        return middleName;
    }

    public void setMiddleName(String middleNameIn) {
        middleName = middleNameIn;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastNameIn) {
        lastName = lastNameIn;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String genderIn) {
        gender = genderIn;
    }

    public long getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDateTime birthDateIn) {
        birthDate = birthDateIn.toEpochSecond(ZoneOffset.UTC);
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String locationIn) {
        location = locationIn;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActiveIn) {
        isActive = isActiveIn;
    }

    public long getCreatedDttm() {
        return createdDttm;
    }

    public void setCreatedDttm(LocalDateTime createdDttmIn) {
        createdDttm = createdDttmIn.toEpochSecond(ZoneOffset.UTC);
    }

}