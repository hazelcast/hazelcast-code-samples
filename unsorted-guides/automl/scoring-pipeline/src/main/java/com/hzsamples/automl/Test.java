package com.hzsamples.automl;

import com.google.protobuf.Int32Value;
import com.google.protobuf.ListValue;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;

import java.util.List;

/**
 * Just some test code to explore exactly how the prediction request is supposed to be built
 */
public class Test {
    public static void main(String []args){
        try {
            String features = "[{\"amount\": 25, \"category\": \"groceries\",\"card present\": false}]";
            ListValue.Builder lvbuiler = ListValue.newBuilder();
            JsonFormat.parser().merge(features, lvbuiler);
            List<Value> instanceList = lvbuiler.getValuesList();
            System.out.println(instanceList);


            Struct.Builder structBuilder = Struct.newBuilder();
            structBuilder.putFields("amount", Value.newBuilder().setNumberValue(25).build());
            structBuilder.putFields("category", Value.newBuilder().setStringValue("groceries").build());
            structBuilder.putFields("card present", Value.newBuilder().setBoolValue(false).build());
            Value result = Value.newBuilder().setStructValue(structBuilder.build()).build();
            System.out.println(result);

            Object []things = new Object[]{32,"Hello", false};
            for(Object thing: things) printThing(thing);

        } catch(Exception x){
            x.printStackTrace(System.err);
            System.exit(1);
        }
    }

    public static void printThing(Integer n){
        System.out.println("Integer: " + n);
    }

    public static void printThing(String n){
        System.out.println("String: " + n);
    }
    public static void printThing(Boolean n){
        System.out.println("Boolean: " + n);
    }
    public static void printThing(Object n){
        System.out.println("Object: " + n);
    }


}
