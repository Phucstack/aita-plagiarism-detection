import java.util.HashMap;
import java.util.Map;

/**
 * Fixture phục vụ demo quét đối soát trên dữ liệu seed.
 * Cài đặt khác biệt (dùng Map và lưu theo mã), dùng làm mẫu đối chiếu độc lập.
 */
public class OrderManager_NhiNH {

    private Map<String, Double> priceByCode = new HashMap<>();
    private Map<String, Integer> quantityByCode = new HashMap<>();

    public void register(String code, double price, int quantity) {
        priceByCode.put(code, price);
        quantityByCode.put(code, quantity);
    }

    public double total() {
        double sum = 0.0;
        for (String code : priceByCode.keySet()) {
            sum += priceByCode.get(code) * quantityByCode.getOrDefault(code, 0);
        }
        return sum;
    }

    public void clear() {
        priceByCode.clear();
        quantityByCode.clear();
    }
}
