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

public final class ToStringPrettyfier {

    private ToStringPrettyfier() {
    }

    @SuppressWarnings("checkstyle:cyclomaticcomplexity")
    private static String prettify(String toStringValue) {
        int depth = 0;

        char[] chars = toStringValue.toCharArray();

        boolean openQuote = false;
        boolean openDoubleQuote = false;

        StringBuilder sb = new StringBuilder(chars.length);
        for (char c : chars) {
            if (c == ',' && !openQuote && !openDoubleQuote) {
                sb.append(',').append('\n');
                indent(sb, depth);
            } else if (c == '{' || c == '[') {
                depth++;
                sb.append(c).append('\n');
                indent(sb, depth);
            } else if (c == '}' || c == ']') {
                depth--;
                sb.append('\n');
                indent(sb, depth);
                sb.append(c);
            } else if (c == '\'') {
                if (!openDoubleQuote) {
                    openQuote = !openQuote;
                }
                sb.append(c);
            } else if (c == '"') {
                if (!openQuote) {
                    openDoubleQuote = !openDoubleQuote;
                }
                sb.append(c);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static void indent(StringBuilder sb, int depth) {
        for (int o = 0; o < depth; o++) {
            sb.append("  ");
        }
    }

    public static String toString(Object value) {
        return prettify(value.toString());
    }
}
