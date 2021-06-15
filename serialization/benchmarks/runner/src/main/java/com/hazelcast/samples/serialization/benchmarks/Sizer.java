package com.hazelcast.samples.serialization.benchmarks;

import java.util.List;

import com.hazelcast.samples.serialization.benchmarks.MyConstants.Kind;
import com.hazelcast.internal.serialization.Data;

/**
 * <p>
 * Serialize and measure the size for each of the serialization types.
 * </p>
 */
public class Sizer {

    public static void size(Kind kind, List<Object> data) {

        int len = -1;
        int lenFirst = -1;
        int count = 0;

        try {
            for (Object datum : data) {
                Data serialized = Util.getSerializationService().toData(datum);

                int itemLength = serialized.toByteArray().length;
                len += itemLength;
                if (count++ == 0) {
                    lenFirst = itemLength;
                }

                /* We should test de-serialization matches.
                 */
                String before = datum.toString();
                String after = Util.getSerializationService().toObject(serialized).toString();
                if (!before.equals(after)) {
                    System.err.println("!before.equals(after)");
                    System.err.println("BEFORE: " + before);
                    System.err.println("AFTER:  " + after);
                }
            }

        } catch (Exception e) {
            System.err.println("Fail for " + kind);
            e.printStackTrace(System.err);
        }

        System.out.printf("%60s : %12s bytes :   first object is %3s bytes%n", kind,
                Util.getNumberFormat().format(len),
                Util.getNumberFormat().format(lenFirst));
    }

}
