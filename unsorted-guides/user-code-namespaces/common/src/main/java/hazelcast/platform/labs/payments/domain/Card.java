package hazelcast.platform.labs.payments.domain;

public class Card  {
    String cardNumber;

    boolean locked;

    // using int just to make arithmetic easier
    int creditLimitDollars;
    int authorizedDollars;

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getCreditLimitDollars() {
        return creditLimitDollars;
    }

    public void setCreditLimitDollars(int creditLimitDollars) {
        this.creditLimitDollars = creditLimitDollars;
    }

    public int getAuthorizedDollars() {
        return authorizedDollars;
    }

    public void setAuthorizedDollars(int authorizedDollars) {
        this.authorizedDollars = authorizedDollars;
    }

    public void addAuthorizedDollars(int amount){
        authorizedDollars += amount;
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardNumber='" + cardNumber + '\'' +
                ", locked=" + locked +
                ", creditLimitDollars=" + creditLimitDollars +
                ", authorizedDollars=" + authorizedDollars +
                '}';
    }

}
