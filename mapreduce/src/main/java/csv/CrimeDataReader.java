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

import model.Crime;
import model.CrimeCategory;
import model.TypeOfCrime;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.util.CsvContext;

class CrimeDataReader extends AbstractDataReader<Crime> {

    @SuppressWarnings("checkstyle:trailingcomment")
    CrimeDataReader() {
        super(new CellProcessor[]{ //
                NOT_NULL, // state
                new TypeOfCrimeCellProcessor(), // type of crime
                new CrimeCategoryCellProcessor(), // crime
                INT, // year
                INT, // count
        }, Crime.class);
    }

    private static class TypeOfCrimeCellProcessor extends CellProcessorAdaptor implements StringCellProcessor {

        @Override
        public Object execute(Object value, CsvContext context) {
            validateInputNotNull(value, context);

            TypeOfCrime result;
            if (value instanceof TypeOfCrime) {
                result = (TypeOfCrime) value;
            } else if (value instanceof String) {
                result = TypeOfCrime.byValue((String) value);
                if (result == null) {
                    throw new SuperCsvCellProcessorException(String.format("typeOfCrime for value '%s' not found", value),
                            context, this);
                }
            } else {
                final String actualClassName = value.getClass().getName();
                throw new SuperCsvCellProcessorException(
                        String.format("the input value should be of type TypeOfCrime or String but is of type %s",
                                actualClassName), context, this
                );
            }
            return next.execute(result, context);
        }
    }

    private static class CrimeCategoryCellProcessor extends CellProcessorAdaptor implements StringCellProcessor {

        @Override
        public Object execute(Object value, CsvContext context) {
            validateInputNotNull(value, context);

            CrimeCategory result;
            if (value instanceof CrimeCategory) {
                result = (CrimeCategory) value;
            } else if (value instanceof String) {
                result = CrimeCategory.byValue((String) value);
                if (result == null) {
                    throw new SuperCsvCellProcessorException(String.format("crimeCategory for value '%s' not found", value),
                            context, this);
                }
            } else {
                final String actualClassName = value.getClass().getName();
                throw new SuperCsvCellProcessorException(
                        String.format("the input value should be of type CrimeCategory or String but is of type %s",
                                actualClassName), context, this
                );
            }
            return next.execute(result, context);
        }
    }
}
