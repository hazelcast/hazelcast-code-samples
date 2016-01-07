/*
 * Copyright (c) 2008-2015, Hazelcast, Inc. All Rights Reserved.
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

import com.hazelcast.config.Config;
import com.hazelcast.config.GroupConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.query.PagingPredicate;
import com.hazelcast.query.impl.predicates.EqualPredicate;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

public class PagingPredicateQuery {

    public static void main(String[] args) {
        Config config = new Config();
        config.setGroupConfig(new GroupConfig(PagingPredicateQuery.class.getName()));
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IMap<Integer, Student> map = hz.getMap("map");

        // fill map
        for (int i = 0; i < 20; i++) {
            ClassName className = (i % 2 == 0 ? ClassName.ClassA : ClassName.ClassB);
            Student student = new Student("Student-" + i, i, className);
            map.put(i, student);
        }

        // an equal predicate to filter out non ClassA students
        EqualPredicate equalPredicate = new EqualPredicate("className", ClassName.ClassA.name());

        // a comparator which helps to sort in descending order of id field
        Comparator<Map.Entry> descendingComparator = new Comparator<Map.Entry>() {
            @Override
            public int compare(Map.Entry e1, Map.Entry e2) {
                Student s1 = (Student) e1.getValue();
                Student s2 = (Student) e2.getValue();
                return s2.getId() - s1.getId();
            }
        };

        // a predicate which filters out non ClassA students, sort them descending order and fetches 4 students for each page
        PagingPredicate pagingPredicate = new PagingPredicate(equalPredicate, descendingComparator, 4);

        // expected result:
        // Page 1 -> Student-18, Student-16, Student-14, Student-12
        // Page 2 -> Student-10, Student-8, Student-6, Student-4
        // Page 3 -> Student-2, Student-0
        Collection<Student> values = map.values(pagingPredicate);
        System.out.print("\nPage 1 -> ");
        for (Student value : values) {
            System.out.print(value + ", ");
        }

        pagingPredicate.nextPage();
        values = map.values(pagingPredicate);
        System.out.print("\nPage 2 -> ");
        for (Student value : values) {
            System.out.print(value + ", ");
        }

        pagingPredicate.nextPage();
        values = map.values(pagingPredicate);
        System.out.print("\nPage 3 -> ");
        for (Student value : values) {
            System.out.print(value + ", ");
        }

        // a predicate which fetches 3 students for each page, natural order (see Student.compareTo()),
        // does not filter out anything
        pagingPredicate = new PagingPredicate(3);

        // since first page is 0, we are requesting the 6th page here
        // expected result:
        // Page 6 -> Student-15, Student-16, Student-17
        pagingPredicate.setPage(5);
        values = map.values(pagingPredicate);
        System.out.print("\n\nPage 6 -> ");
        for (Student value : values) {
            System.out.print(value + ", ");
        }

        System.out.println();
        Hazelcast.shutdownAll();
    }
}
