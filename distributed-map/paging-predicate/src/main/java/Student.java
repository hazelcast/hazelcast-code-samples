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

import java.io.Serializable;

@SuppressWarnings("unused")
public class Student implements Serializable, Comparable<Student> {

    private final String name;
    private final int id;
    private final ClassName className;

    public Student(String name, int id, ClassName className) {
        this.name = name;
        this.id = id;
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public ClassName getClassName() {
        return className;
    }

    @Override
    public int compareTo(Student other) {
        return this.id - other.id;
    }

    @Override
    public String toString() {
        return "Student{"
                + "name='" + name + '\''
                + ", id=" + id
                + ", className=" + className
                + '}';
    }
}
