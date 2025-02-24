/*
 *
 *  Copyright (c) 2008-2018, Hazelcast, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

import com.hazelcast.map.MapLoader;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EchoMapLoader implements MapLoader<String, String> {
    @Override
    public String load(String key) {
        return key;
    }

    @Override
    public Map<String, String> loadAll(Collection<String> keys) {
        Map<String, String> result = new HashMap<>();
        for (String key : keys) {
            result.put(key, key);
        }
        return result;
    }

    @Override
    public Iterable<String> loadAllKeys() {
        return Arrays.asList(new String[] {"1", "2", "3"});
    }
}
