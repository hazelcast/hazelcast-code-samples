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

import com.hazelcast.mapreduce.Combiner;
import com.hazelcast.mapreduce.CombinerFactory;

public class SalaryCombinerFactory implements CombinerFactory<String, Integer, SalaryTuple> {

    @Override
    public Combiner<Integer, SalaryTuple> newCombiner(String key) {
        return new SalaryCombiner();
    }

    private static class SalaryCombiner extends Combiner<Integer, SalaryTuple> {

        private int count;
        private int amount;

        @Override
        public void combine(Integer value) {
            count++;
            amount += value;
        }

        @Override
        public SalaryTuple finalizeChunk() {
            int count = this.count;
            int amount = this.amount;
            this.count = 0;
            this.amount = 0;

            SalaryTuple tuple = new SalaryTuple();
            tuple.setCount(count);
            tuple.setAmount(amount);
            return tuple;
        }
    }
}
