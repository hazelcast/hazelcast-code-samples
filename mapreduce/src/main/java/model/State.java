/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package model;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

@SuppressWarnings({"unused", "checkstyle:methodcount"})
public class State implements DataSerializable {

    private int id;
    private String name;
    private String abbreviation;
    private String country;
    private String type;
    private int sort;
    private String status;
    private String occupied;
    private String notes;
    private int fipsState;
    private String assocPress;
    private String standardFederalRegion;
    private int censusRegion;
    private String censusRegionName;
    private int censusDivision;
    private String censusDevisionName;
    private int circuitCourt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOccupied() {
        return occupied;
    }

    public void setOccupied(String occupied) {
        this.occupied = occupied;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getFipsState() {
        return fipsState;
    }

    public void setFipsState(int fipsState) {
        this.fipsState = fipsState;
    }

    public String getAssocPress() {
        return assocPress;
    }

    public void setAssocPress(String assocPress) {
        this.assocPress = assocPress;
    }

    public String getStandardFederalRegion() {
        return standardFederalRegion;
    }

    public void setStandardFederalRegion(String standardFederalRegion) {
        this.standardFederalRegion = standardFederalRegion;
    }

    public int getCensusRegion() {
        return censusRegion;
    }

    public void setCensusRegion(int censusRegion) {
        this.censusRegion = censusRegion;
    }

    public String getCensusRegionName() {
        return censusRegionName;
    }

    public void setCensusRegionName(String censusRegionName) {
        this.censusRegionName = censusRegionName;
    }

    public int getCensusDivision() {
        return censusDivision;
    }

    public void setCensusDivision(int censusDivision) {
        this.censusDivision = censusDivision;
    }

    public String getCensusDevisionName() {
        return censusDevisionName;
    }

    public void setCensusDevisionName(String censusDevisionName) {
        this.censusDevisionName = censusDevisionName;
    }

    public int getCircuitCourt() {
        return circuitCourt;
    }

    public void setCircuitCourt(int circuitCourt) {
        this.circuitCourt = circuitCourt;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeInt(id);
        out.writeUTF(name);
        out.writeUTF(abbreviation);
        out.writeUTF(country);
        out.writeUTF(type);
        out.writeInt(sort);
        out.writeUTF(status);
        out.writeUTF(occupied);
        out.writeUTF(notes);
        out.writeInt(fipsState);
        out.writeUTF(assocPress);
        out.writeUTF(standardFederalRegion);
        out.writeInt(censusRegion);
        out.writeUTF(censusRegionName);
        out.writeInt(censusDivision);
        out.writeUTF(censusDevisionName);
        out.writeInt(circuitCourt);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        id = in.readInt();
        name = in.readUTF();
        abbreviation = in.readUTF();
        country = in.readUTF();
        type = in.readUTF();
        sort = in.readInt();
        status = in.readUTF();
        occupied = in.readUTF();
        notes = in.readUTF();
        fipsState = in.readInt();
        assocPress = in.readUTF();
        standardFederalRegion = in.readUTF();
        censusRegion = in.readInt();
        censusRegionName = in.readUTF();
        censusDivision = in.readInt();
        censusDevisionName = in.readUTF();
        circuitCourt = in.readInt();
    }

    @Override
    public String toString() {
        return "State{"
                + "id=" + id
                + ", name='" + name + '\''
                + ", abbreviation='" + abbreviation + '\''
                + ", country='" + country + '\''
                + ", type='" + type + '\''
                + ", sort=" + sort
                + ", status='" + status + '\''
                + ", occupied='" + occupied + '\''
                + ", notes='" + notes + '\''
                + ", fipsState=" + fipsState
                + ", assocPress='" + assocPress + '\''
                + ", standardFederalRegion='" + standardFederalRegion + '\''
                + ", censusRegion=" + censusRegion
                + ", censusRegionName='" + censusRegionName + '\''
                + ", censusDivision=" + censusDivision
                + ", censusDevisionName='" + censusDevisionName + '\''
                + ", circuitCourt=" + circuitCourt
                + '}';
    }
}
