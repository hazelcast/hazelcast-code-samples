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

package csv;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import model.Crime;
import model.Person;
import model.SalaryYear;
import model.State;

import java.io.InputStream;
import java.util.List;

public final class ReaderHelper {

    private static final ClassLoader CLASS_LOADER = ReaderHelper.class.getClassLoader();

    private ReaderHelper() {
    }

    public static void read(HazelcastInstance hazelcastInstance) throws Exception {
        readStates(hazelcastInstance);
        readPeople(hazelcastInstance);
        readCrimes(hazelcastInstance);
        readSalary(hazelcastInstance);
    }

    private static void readStates(HazelcastInstance hazelcastInstance) throws Exception {
        StateDataReader stateDataReader = new StateDataReader();
        InputStream is = CLASS_LOADER.getResourceAsStream("state_table.csv");
        try {
            IMap<Integer, State> statesMap = hazelcastInstance.getMap("states");
            List<State> states = stateDataReader.read(is);
            for (State state : states) {
                statesMap.put(state.getId(), state);
            }
        } finally {
            is.close();
        }
    }

    private static void readPeople(HazelcastInstance hazelcastInstance) throws Exception {
        PersonDataReader personDataReader = new PersonDataReader();
        InputStream is = CLASS_LOADER.getResourceAsStream("us-500.csv");
        try {
            IList<Person> personsList = hazelcastInstance.getList("persons");
            List<Person> persons = personDataReader.read(is);
            personsList.addAll(persons);
        } finally {
            is.close();
        }
    }

    private static void readSalary(HazelcastInstance hazelcastInstance) throws Exception {
        SalaryDataReader salaryDataReader = new SalaryDataReader();
        InputStream is = CLASS_LOADER.getResourceAsStream("salary.csv");
        try {
            IMap<String, SalaryYear> salariesMap = hazelcastInstance.getMap("salaries");
            List<SalaryYear> salaries = salaryDataReader.read(is);
            for (SalaryYear salary : salaries) {
                salariesMap.put(salary.getEmail(), salary);
            }
        } finally {
            is.close();
        }
    }

    private static void readCrimes(HazelcastInstance hazelcastInstance) throws Exception {
        CrimeDataReader crimeDataReader = new CrimeDataReader();
        InputStream is = CLASS_LOADER.getResourceAsStream("CrimeStatebyState.csv");
        try {
            IList<Crime> crimesList = hazelcastInstance.getList("crimes");
            List<Crime> crimes = crimeDataReader.read(is);
            crimesList.addAll(crimes);
        } finally {
            is.close();
        }
    }
}
