package cn.lywyw.service;

import cn.lywyw.dao.OrderRepository;
import cn.lywyw.domain.Order;
import cn.lywyw.domain.Product;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ShopService {
    public static final Product KEYBOARD = new Product("100010090091", "机械键盘", new BigDecimal("1.68"));
    private final OrderRepository orders;

    public ShopService(OrderRepository orders) {
        this.orders = orders;
    }

    public Order create(String owner, String productId, UUID request) {
        if (!KEYBOARD.id().equals(productId) || request == null) {
            throw new IllegalArgumentException("请选择有效商品并提供请求编号");
        }
        UUID id = UUID.randomUUID();
        try {
            orders.insert(id, owner, request, KEYBOARD);
        } catch (DuplicateKeyException duplicate) {
            return orders.byRequest(owner, request).orElseThrow(() -> duplicate);
        }
        return get(owner, id);
    }

    public Order get(String owner, UUID id) {
        return orders.byId(owner, id).orElseThrow(() -> new NoSuchElementException("订单不存在"));
    }

    public Order finish(String owner, UUID id, boolean pay) {
        String target = pay ? "pay_success" : "close";
        orders.finish(owner, id, target);
        Order order = get(owner, id);
        if (!target.equals(order.status())) {
            throw new IllegalStateException("订单已结束，无法变更");
        }
        return order;
    }
}
