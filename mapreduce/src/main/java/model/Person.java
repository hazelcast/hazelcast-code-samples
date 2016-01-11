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

import com.hazelcast.core.PartitionAware;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

@SuppressWarnings("unused")
public class Person implements DataSerializable, PartitionAware<String> {

    private String firstName;
    private String lastName;
    private String companyName;
    private String address;
    private String city;
    private String county;
    private String state;
    private int zip;
    private String phone1;
    private String phone2;
    private String email;
    private String web;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getZip() {
        return zip;
    }

    public void setZip(int zip) {
        this.zip = zip;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(firstName);
        out.writeUTF(lastName);
        out.writeUTF(companyName);
        out.writeUTF(address);
        out.writeUTF(city);
        out.writeUTF(county);
        out.writeUTF(state);
        out.writeInt(zip);
        out.writeUTF(phone1);
        out.writeUTF(phone2);
        out.writeUTF(email);
        out.writeUTF(web);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        firstName = in.readUTF();
        lastName = in.readUTF();
        companyName = in.readUTF();
        address = in.readUTF();
        city = in.readUTF();
        county = in.readUTF();
        state = in.readUTF();
        zip = in.readInt();
        phone1 = in.readUTF();
        phone2 = in.readUTF();
        email = in.readUTF();
        web = in.readUTF();
    }

    @Override
    public String getPartitionKey() {
        return email;
    }

    @Override
    public String toString() {
        return "Person{"
                + "firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", companyName='" + companyName + '\''
                + ", address='" + address + '\''
                + ", city='" + city + '\''
                + ", county='" + county + '\''
                + ", state='" + state + '\''
                + ", zip=" + zip
                + ", phone1='" + phone1 + '\''
                + ", phone2='" + phone2 + '\''
                + ", email='" + email + '\''
                + ", web='" + web + '\''
                + '}';
    }
}
