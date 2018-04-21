/*
 * Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
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

package configvalidation;

import com.hazelcast.cardinality.CardinalityEstimator;
import com.hazelcast.config.CardinalityEstimatorConfig;
import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.MergePolicyConfig;
import mergepolicies.MergeIntegerValuesMergePolicy;

/**
 * Shows that {@link CardinalityEstimator}
 * doesn't support custom merge policies.
 */
public class CardinalityEstimatorExample {

    public static void main(String[] args) {
        MergePolicyConfig mergePolicyConfig = new MergePolicyConfig()
                .setPolicy(MergeIntegerValuesMergePolicy.class.getName());

        try {
            new CardinalityEstimatorConfig("default")
                    .setMergePolicyConfig(mergePolicyConfig);
        } catch (InvalidConfigurationException e) {
            System.out.println("The CardinalityEstimator doesn't allow custom merge policies: " + e.getMessage());
        }
    }
}
