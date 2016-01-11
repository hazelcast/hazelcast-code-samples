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

package wordcount;

import com.hazelcast.mapreduce.Reducer;
import com.hazelcast.mapreduce.ReducerFactory;

public class WordcountReducerFactory implements ReducerFactory<String, Integer, Integer> {

    @Override
    public Reducer<Integer, Integer> newReducer(String key) {
        return new WordcountReducer();
    }

    private static class WordcountReducer extends Reducer<Integer, Integer> {

        private volatile int count;

        @Override
        public void reduce(Integer value) {
            // use with and without Combiner to show combining phase!
            // System.out.println("Retrieved value: " + value);
            count += value;
        }

        @Override
        public Integer finalizeReduce() {
            return count == 0 ? null : count;
        }
    }
}
