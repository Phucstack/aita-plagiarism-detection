import java.util.LinkedList;
import java.util.Queue;

/**
 * Fixture phục vụ demo quét đối soát trên dữ liệu seed.
 * Cài đặt theo hướng hàng đợi xử lý đơn, khác hoàn toàn hai bài còn lại.
 */
public class OrderManager_TienN {

    private Queue<String> pendingOrders = new LinkedList<>();
    private int processed = 0;

    public void enqueue(String orderCode) {
        pendingOrders.offer(orderCode);
    }

    public String dequeue() {
        String orderCode = pendingOrders.poll();
        if (orderCode != null) {
            processed++;
        }
        return orderCode;
    }

    public int pending() {
        return pendingOrders.size();
    }

    public int processedCount() {
        return processed;
    }
}
