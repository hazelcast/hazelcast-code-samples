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

package csv;

import model.State;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;

class StateDataReader extends AbstractDataReader<State> {

    @SuppressWarnings("checkstyle:trailingcomment")
    StateDataReader() {
        super(new CellProcessor[]{
                INT, // id
                NOT_NULL, // name
                NOT_NULL, // abbreviation
                NOT_NULL, // country
                NOT_NULL, // type
                INT, // sort
                NOT_NULL, // status
                NOT_NULL, // occupied
                new Optional(), // notes
                INT, // fips_state
                NOT_NULL, // assoc_press
                NOT_NULL, // standard_federal_region
                INT, // census_region
                NOT_NULL, // census_region_name
                INT, // census_division
                NOT_NULL, // census_devision_name
                INT, // circuit_court
        }, State.class);
    }
}
