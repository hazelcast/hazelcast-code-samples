/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.samples.sql.hazdb;

import java.util.Objects;

/**
 * <p>Shared utility functions.
 * </p>
 */
public class Utils {


    /**
     * <p>Ensure text isn't JSON, so can be a text field.
     * </p>
     *
     * @param input May be null
     * @return
     */
    public static String makeText(Object json) {
        String[] tokens = Objects.toString(json)
                .replaceAll("\"", "'")
                .replaceAll(" +", " ")
                .split(System.getProperty("line.separator"));

        String result = tokens[0].trim();

        for (int i = 1; i < tokens.length; i++) {
            result += "+" + tokens[i].trim();
        }

        return result;
    }
}
