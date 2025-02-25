package hazelcast.platform.labs.payments.domain;

import com.fasterxml.jackson.annotation.JsonSetter;

import java.io.Serializable;

/*
 * Represents a Credit Card transaction.
 *
 * Note that an instance of this class occur inside of TransactionEntryProcessor,
 * which means that it must be java.io.Serializable (otherwise we could use
 * a more efficient form of Serialization like Compact).
 */
public class Transaction implements Serializable {
    public enum Status {NEW, INVALID_CARD, DECLINED_BIG_TXN, DECLINED_LOCKED, DECLINED_OVER_AUTH_LIMIT, APPROVED};
    private String cardNumber;
    private String transactionId;
    private int amount;
    private String merchantId;
    private Status status;

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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "cardNumber='" + cardNumber + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", amount=" + amount +
                ", merchantId='" + merchantId + '\'' +
                ", status=" + status +
                '}';
    }
}
