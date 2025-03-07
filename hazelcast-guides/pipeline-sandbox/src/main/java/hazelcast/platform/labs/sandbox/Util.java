package hazelcast.platform.labs.sandbox;

public class Util {
    public static <T> void log(String stage, int index, T event ){
        // NOTE: this routine assumes the length of event.toString is 8
        if (!"ABC".contains(stage)) throw new RuntimeException("Invalid value for stage");
        StringBuilder sb = new StringBuilder();
        String timeStr = "" + System.currentTimeMillis();
        sb.append(timeStr);
        sb.append(" ".repeat(16 - timeStr.length()));

        String columnContent;
        for(String s: new String []{"A","B","C"}){
            for(int i=0;i<3;++i) {
                if (stage.equals(s) && index == i){
                    sb.append(event.toString());
                } else {
                    sb.append(" ".repeat(8));
                }
            }
        }

        System.out.println(sb);
    }

    public static void printHeader(){
        StringBuilder sb = new StringBuilder("event timestamp ");
        for(String stage: new String []{"A","B","C"}){
            for(int i=0;i<3;++i) addHeader(sb, stage, i);
        }
        System.out.println(sb);
    }

    public static void addHeader(StringBuilder sb, String stage, int index){
        if (stage.length() > 5) throw new RuntimeException("stage name too long");
        sb.append(stage).append("[").append(index).append("]").append(" ".repeat(5 - stage.length()));
    }
    // columns are at 16,24,32 40,48,56 64,72,80

    private static <T> void swap(T []a, int i, int j){
        T x = a[i];
        a[i] = a[j];
        a[j] = x;
    }

    private static <T> void reverse(T []a){
        int limit = a.length/2;  // integer truncation is intentional
        for(int i = 0; i < limit; ++i){
            swap(a, i, a.length - 1 - i);
        }
    }

}
