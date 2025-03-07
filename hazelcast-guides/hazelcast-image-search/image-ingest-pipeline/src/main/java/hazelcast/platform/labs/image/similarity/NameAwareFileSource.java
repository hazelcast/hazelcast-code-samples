package hazelcast.platform.labs.image.similarity;

import com.hazelcast.jet.datamodel.Tuple2;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class NameAwareFileSource implements AutoCloseable {

    private Logger log = LogManager.getLogger(NameAwareFileSource.class);

    private final DirectoryStream<Path> dirStream;
    private final Iterator<Path> iter;

    private final int slice;
    private final int modulus;
    public NameAwareFileSource(String dir, String glob, int slice, int modulus){
        try {
            this.dirStream = Files.newDirectoryStream(Path.of(dir), glob);
            this.iter = dirStream.iterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.slice = slice;
        this.modulus = modulus;
    }

    /*
     * Returns an entry having the name of the next file as the key part and the contents of the next file as a byte[]
     *
     * Returns null if there are no more files.
     */
    public Tuple2<String, byte[]> next(){
        Path path = null;
        while(true){
            if (!iter.hasNext()) {
                break;
            }

            Path p = iter.next();
            if (modulus < 2) {
                path = p;
                break;
            } else {
                int m = p.toString().hashCode() % modulus;
                if (m < 0) m+= modulus;  // make m non-negative
                if (m == slice) {
                    path = p;
                    break;
                }
            }
        }
        if (path == null){
            return null;
        }

        try {
            log.info("Returning contents of " + path.toString());
            return Tuple2.tuple2(path.toString(), Files.readAllBytes(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        dirStream.close();
    }
}
