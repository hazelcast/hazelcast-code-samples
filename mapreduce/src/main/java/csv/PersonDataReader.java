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

import model.Person;
import org.supercsv.cellprocessor.ift.CellProcessor;

class PersonDataReader extends AbstractDataReader<Person> {

    @SuppressWarnings("checkstyle:trailingcomment")
    PersonDataReader() {
        super(new CellProcessor[]{ //
                NOT_NULL, // first_name
                NOT_NULL, // last_name
                NOT_NULL, // company_name
                NOT_NULL, // address
                NOT_NULL, // city
                NOT_NULL, // county
                NOT_NULL, // state
                INT, // zip
                NOT_NULL, // phone1
                NOT_NULL, // phone2
                NOT_NULL, // email
                NOT_NULL, // web
        }, Person.class);
    }
}
