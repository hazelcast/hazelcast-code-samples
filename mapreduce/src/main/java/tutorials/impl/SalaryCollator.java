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

package tutorials.impl;

import com.hazelcast.mapreduce.Collator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SalaryCollator implements Collator<Map.Entry<String, Integer>, List<Map.Entry<String, Integer>>> {

    @Override
    public List<Map.Entry<String, Integer>> collate(Iterable<Map.Entry<String, Integer>> values) {
        List<Map.Entry<String, Integer>> result = new ArrayList<Map.Entry<String, Integer>>();
        for (Map.Entry<String, Integer> value : values) {
            result.add(value);
        }
        Collections.sort(result, new AvgSalaryComparator());
        return result;
    }

    private static class AvgSalaryComparator implements Comparator<Map.Entry<String, Integer>> {

        @Override
        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
            int value1 = o1.getValue();
            int value2 = o2.getValue();
            return (value2 < value1) ? -1 : ((value2 == value1) ? 0 : 1);
        }
    }
}
