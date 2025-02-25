package hazelcast.platform.labs.payments.domain;

import com.fasterxml.jackson.annotation.JsonSetter;

public class Transaction {
    private String cardNumber;
    private String transactionId;
    private int amount;
    private String merchantId;

    public String getCardNumber() {
        return cardNumber;
    }

    @JsonSetter("card_number")
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getTransactionId() {
        return transactionId;
    }

    @JsonSetter("transaction_id")
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getAmount() {
        return amount;
    }

    @JsonSetter("amount")
    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMerchantId() {
        return merchantId;
    }

    @JsonSetter("merchant_id")
    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "cardNumber='" + cardNumber + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", merchantId='" + merchantId + '\'' +
                '}';
    }
}
