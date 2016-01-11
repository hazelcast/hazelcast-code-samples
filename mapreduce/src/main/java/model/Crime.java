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

@SuppressWarnings("unused")
public class Crime implements DataSerializable {

    private String state;
    private TypeOfCrime typeOfCrime;
    private CrimeCategory crime;
    private int year;
    private int count;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public TypeOfCrime getTypeOfCrime() {
        return typeOfCrime;
    }

    public void setTypeOfCrime(TypeOfCrime typeOfCrime) {
        this.typeOfCrime = typeOfCrime;
    }

    public CrimeCategory getCrime() {
        return crime;
    }

    public void setCrime(CrimeCategory crime) {
        this.crime = crime;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(state);
        out.writeInt(typeOfCrime.ordinal());
        out.writeInt(crime.ordinal());
        out.writeInt(year);
        out.writeInt(count);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        state = in.readUTF();
        typeOfCrime = TypeOfCrime.byOrdinal(in.readInt());
        crime = CrimeCategory.byOrdinal(in.readInt());
        year = in.readInt();
        count = in.readInt();
    }

    @Override
    public String toString() {
        return "Crime{"
                + "state='" + state + '\''
                + ", typeOfCrime=" + typeOfCrime
                + ", crime=" + crime
                + ", year=" + year
                + ", count=" + count
                + '}';
    }
}
