package hazelcast.platform.labs.jet.connectors;

import com.hazelcast.jet.datamodel.Tuple2;
import com.hazelcast.jet.pipeline.SourceBuilder;
import com.hazelcast.jet.pipeline.StreamSource;

/*
 * Creates a non-distributed Source that watches a directory and emits
 * tuples of (event type, file name).
 *
 * The event types are defined by DirectoryWatcher.EventType and can be
 * CREATE, UPDATE, DELETE or UNKNOWN.
 *
 * The file name is just the name of the file that was created/updated/deleted.
 * The file name is not a full path.
 *
 * This source does not save any information to the snapshot.  Also, note that
 * it does not create events for files that are already in the directory,
 * only for files that are added/updated/removed
 */
public class DirectoryWatcherSourceBuilder {
    public static StreamSource<Tuple2<DirectoryWatcher.EventType, String>> newDirectoryWatcher(String dir, String suffix){
        return SourceBuilder.stream("Directory Watcher Source", ctx ->  new DirectoryWatcher(dir, suffix))
                .<Tuple2<DirectoryWatcher.EventType, String>>fillBufferFn((watcher, buffer) -> {
                    for (Tuple2<DirectoryWatcher.EventType, String> event: watcher.poll()) buffer.add(event);
                }).destroyFn( DirectoryWatcher::close)
                .build();
    }
}
