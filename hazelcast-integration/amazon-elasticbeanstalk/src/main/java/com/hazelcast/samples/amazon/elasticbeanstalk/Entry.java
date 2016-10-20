/*
 *
 *  Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.samples.amazon.elasticbeanstalk;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author László Csontos
 */
public class Entry {

    public static final Entry NULL_ENTRY = new Entry();

    private final String key;
    private final String value;

    public Entry() {
        this(null, null);
    }

    @JsonCreator
    public Entry(@JsonProperty("key") String key, @JsonProperty("value") String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Entry)) {
            return false;
        }

        Entry entry = (Entry) obj;
        return (key != null && key.equals(entry.key));
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
