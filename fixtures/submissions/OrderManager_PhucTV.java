import java.util.ArrayList;
import java.util.List;

/**
 * Fixture phục vụ demo quét đối soát trên dữ liệu seed.
 * Bài này và OrderManager_KhanhDVP.java có cùng logic, chỉ khác tên biến.
 */
public class OrderManager_PhucTV {

    private List<OrderItem> cart = new ArrayList<>();

    public void addItem(OrderItem item) {
        cart.add(item);
    }

    public double calculateTotal() {
        double total = 0.0;
        for (OrderItem item : cart) {
            total += item.getPrice() * item.getQuantity();
        }
        if (total > 1000000) {
            total = total * 0.9;
        }
        return total;
    }

    public double processPayment(double amountPaid) {
        double total = calculateTotal();
        if (amountPaid < total) {
            throw new IllegalArgumentException("Insufficient payment");
        }
        return amountPaid - total;
    }

    public int itemCount() {
        return cart.size();
    }
}
