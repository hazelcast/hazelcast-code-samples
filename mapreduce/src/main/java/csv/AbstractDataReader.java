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

import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

abstract class AbstractDataReader<T> implements DataReader<T> {

    static final CellProcessor INT = new ParseInt();
    static final CellProcessor NOT_NULL = new NotNull();

    private final CellProcessor[] cellProcessors;
    private final Class<T> type;

    AbstractDataReader(CellProcessor[] cellProcessors, Class<T> type) {
        this.cellProcessors = cellProcessors;
        this.type = type;
    }

    @Override
    public List<T> read(InputStream is) throws Exception {
        List<T> elements = new ArrayList<T>();
        ICsvBeanReader reader = new CsvBeanReader(new InputStreamReader(is), CsvPreference.EXCEL_PREFERENCE);
        try {
            String[] headers = mapHeaderNames(reader.getHeader(true));

            T element;
            while ((element = reader.read(type, headers, cellProcessors)) != null) {
                elements.add(element);
            }
        } finally {
            reader.close();
        }
        return elements;
    }

    private String[] mapHeaderNames(String[] headers) {
        for (int i = 0; i < headers.length; i++) {
            headers[i] = mapHeaderName(headers[i]);
        }
        return headers;
    }

    private String mapHeaderName(String header) {
        StringBuilder sb = new StringBuilder(header.length());

        char[] chars = header.toCharArray();

        boolean nextUpperCase = false;
        for (char c : chars) {
            if (Character.isWhitespace(c) || c == '_') {
                nextUpperCase = true;
                continue;
            } else {
                sb.append(nextUpperCase ? Character.toUpperCase(c) : c);
            }
            nextUpperCase = false;
        }

        return sb.toString();
    }
}
