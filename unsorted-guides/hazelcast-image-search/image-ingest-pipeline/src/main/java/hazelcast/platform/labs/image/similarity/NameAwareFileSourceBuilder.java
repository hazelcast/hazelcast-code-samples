package hazelcast.platform.labs.image.similarity;

import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.BatchSource;
import com.hazelcast.jet.pipeline.SourceBuilder;

import java.io.File;

/*
 * Builds a BatchSource that emits a Tuple2 consisting of the file name and the file contents as a byte[]
 */
public class NameAwareFileSourceBuilder {
    public static BatchSource<Tuple2<String, byte[]>>  newFileSource(
            String dir,
            String glob,
            int batchSize){
        return SourceBuilder.batch(
                "NameAwareSource(" + File.separator + glob + ")",
                ctx -> new NameAwareFileSource(dir, glob, 0, 0))
                .<Tuple2<String, byte[]>>fillBufferFn( (src, buf) -> fillBuffer(src, buf, batchSize))
                .destroyFn(NameAwareFileSource::close)
                .build();
    }
    public static BatchSource<Tuple2<String, byte[]>>  newDistributedFileSource(
            String dir,
            String glob,
            int batchSize){
        return SourceBuilder.batch(
                        "NameAwareSource(" + File.separator + glob + ")",
                        ctx -> new NameAwareFileSource(dir, glob, ctx.globalProcessorIndex(), ctx.totalParallelism()))
                .<Tuple2<String, byte[]>>fillBufferFn( (src, buf) -> fillBuffer(src, buf, batchSize))
                .destroyFn(NameAwareFileSource::close)
                .distributed(1)
                .build();
    }

    private static void fillBuffer(
            NameAwareFileSource source,
            SourceBuilder.SourceBuffer<Tuple2<String, byte[]>> buffer,
            int batchSize){
        int returnCount = 0;
        while (returnCount < batchSize ){
            Tuple2<String, byte[]> entry = source.next();
            if (entry == null) {
                buffer.close();
                break;
            }
            buffer.add(entry);
            returnCount += 1;
        }
    }
}
