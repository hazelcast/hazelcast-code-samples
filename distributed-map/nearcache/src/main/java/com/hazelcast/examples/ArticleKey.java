package com.hazelcast.examples;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;

public class ArticleKey implements DataSerializable {

    private int key;

    public ArticleKey(int key) {
        this.key = key;
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        System.out.println("Called writeData() for key " + key);
        out.writeInt(key);
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        System.out.println("Called readData() for key " + key);
        key = in.readInt();
    }
}
