enum MySerializationConstants {

    PERSON_TYPE(1),
    CAR_TYPE(2),
    EXTENDED_ARRAYLIST_TYPE(3);

    private int id;

    MySerializationConstants(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
