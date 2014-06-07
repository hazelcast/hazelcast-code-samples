import java.io.Serializable;

public final class Order implements Serializable {
    public final long orderId;
    public final long customerId;
    public final long articleId;

    public Order(long orderId, long customerId, long articleId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.articleId = articleId;
    }
}