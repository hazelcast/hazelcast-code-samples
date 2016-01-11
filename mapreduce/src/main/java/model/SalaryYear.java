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
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SalaryYear implements DataSerializable {

    private String email;
    private int year;
    private List<SalaryMonth> months;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<SalaryMonth> getMonths() {
        return months;
    }

    public void setMonths(List<SalaryMonth> months) {
        this.months = months;
    }

    public int getAnnualSalary() {
        int sum = 0;
        for (SalaryMonth salaryMonth : getMonths()) {
            sum += salaryMonth.getSalary();
        }
        return sum;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        out.writeUTF(email);
        out.writeInt(year);
        out.writeInt(months.size());
        for (SalaryMonth salaryMonth : months) {
            salaryMonth.writeData(out);
        }
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        email = in.readUTF();
        year = in.readInt();
        int length = in.readInt();
        months = new ArrayList<SalaryMonth>(12);
        for (int i = 0; i < length; i++) {
            SalaryMonth salaryMonth = new SalaryMonth();
            salaryMonth.readData(in);
            months.add(salaryMonth);
        }
    }

    @Override
    public String toString() {
        return "SalaryYear{"
                + "email='" + email + '\''
                + ", year=" + year
                + ", months=" + months
                + '}';
    }
}
