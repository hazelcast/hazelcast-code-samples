package hazelcast.platform.labs.sandbox;

/*
 * supplies the content of the list in order
 *
 * when the list is empty it throws an OutOfDataException which, if
 * unhandled, will cause the pipeline to exit
 *
 */

public class TestEventSource<T> {

    private final T[] events;
    private int i;   // the index of the item that next will be returned next time it is called
    private final int modulus;
    private final int instanceNum;

    public TestEventSource(T []events, int instanceNum, int totalInstances){
        this.events = events;
        this.i = instanceNum;
        this.modulus = totalInstances;
        this.instanceNum = instanceNum;
    }

    T next()   {
        if (i >= events.length){
            return null;
        } else {
            T result = events[i];
            i += modulus;
            Util.log("A", instanceNum, result);
            return result;
        }
    }

//    public static class OutOfDataException extends Exception {
//    }

}
