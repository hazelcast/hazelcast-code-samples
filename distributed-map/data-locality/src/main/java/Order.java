import java.io.Serializable;

final class Order implements Serializable {

    private final long orderId;
    private final long customerId;
    private final long articleId;

    Order(long orderId, long customerId, long articleId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.articleId = articleId;
    }

    @Override
    public String toString() {
        return "Order{"
                + "orderId=" + orderId
                + ", customerId=" + customerId
                + ", articleId=" + articleId
                + '}';
    }
}
