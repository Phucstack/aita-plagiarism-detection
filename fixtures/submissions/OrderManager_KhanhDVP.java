import java.util.ArrayList;
import java.util.List;

/**
 * Fixture phục vụ demo quét đối soát trên dữ liệu seed.
 * Cùng logic với OrderManager_PhucTV.java, đã đổi tên biến và tên phương thức phụ
 * — trường hợp điển hình mà lõi đối soát cần phát hiện.
 */
public class OrderManager_KhanhDVP {

    private List<OrderItem> basket = new ArrayList<>();

    public void insert(OrderItem element) {
        basket.add(element);
    }

    public double computeSum() {
        double finalCost = 0.0;
        for (OrderItem element : basket) {
            finalCost += element.getPrice() * element.getQuantity();
        }
        if (finalCost > 1000000) {
            finalCost = finalCost * 0.9;
        }
        return finalCost;
    }

    public double handleTransaction(double moneyReceived) {
        double finalCost = computeSum();
        if (moneyReceived < finalCost) {
            throw new IllegalArgumentException("Not enough money");
        }
        return moneyReceived - finalCost;
    }

    public int size() {
        return basket.size();
    }
}
