/*
 * Copyright (c) 2008-2023, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.cloud.model;

import com.hazelcast.nio.serialization.compact.CompactReader;
import com.hazelcast.nio.serialization.compact.CompactSerializer;
import com.hazelcast.nio.serialization.compact.CompactWriter;

public class CitySerializer implements CompactSerializer<City> {
    @Override
    public City read(CompactReader compactReader) {
        return new City(compactReader.readString("country"),
                        compactReader.readString("city"),
                        compactReader.readInt32("population"));
    }

    @Override
    public void write(CompactWriter compactWriter, City city) {
        compactWriter.writeString("country", city.getCountry());
        compactWriter.writeString("city", city.getCity());
        compactWriter.writeInt32("population", city.getPopulation());
    }

    @Override
    public String getTypeName() {
        return "city";
    }

    @Override
    public Class<City> getCompactClass() {
        return City.class;
    }
}
