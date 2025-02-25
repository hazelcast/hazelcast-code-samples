package hazelcast.platform.labs.payments.domain;

import com.github.javafaker.Faker;

public class FakeData {

    private static final Faker faker = new Faker();
    public static Card card(){
        Card result = new Card();
        String cc = faker.finance().creditCard();
        while(cc.length() != 19)
            cc = faker.finance().creditCard();

        result.setCardNumber(cc);
        result.setLocked( faker.random().nextDouble() < .1);

        result.setCreditLimitDollars(faker.random().nextInt(1,100) * 100);
        result.setAuthorizedDollars(faker.random().nextInt(0, result.getCreditLimitDollars()));

        return result;
    }
}
