package org.hazelcast.jet.demo.types;

/**
 * The wake turbulence category of the aircraft
 */
public enum WakeTurbulanceCategory {
    NONE(0),
    LIGHT(1),
    SMALL(2),
    MEDIUM(3),
    LARGE(4),
    HIGHVORTEXLARGE(5),
    HEAVY(6),
    HIGHPERFORMANCE(7),
    ROTORCRAFT(8);


    private final int id;

    WakeTurbulanceCategory(int id) {
        this.id = id;
    }

    public static WakeTurbulanceCategory fromId(int id) {
        for (WakeTurbulanceCategory type : WakeTurbulanceCategory.values()) {
            if (type.id == id) {
                return type;
            }
        }
        return NONE;
    }

}
