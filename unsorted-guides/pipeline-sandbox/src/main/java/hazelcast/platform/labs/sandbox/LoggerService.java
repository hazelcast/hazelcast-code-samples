package hazelcast.platform.labs.sandbox;

import java.io.Serializable;

public class LoggerService<T> implements Serializable {
    private int globalIndex;
    private String name;

    public LoggerService(String name, int globalIndex) {
        this.name = name;
        this.globalIndex = globalIndex;
    }

    public LoggerService(){}

    T map(T in){
//        System.out.println("t=" + System.currentTimeMillis() + " " + name + "[" + globalIndex + "] " + in);
        Util.log(name, globalIndex, in);
        return in;
    }

}
