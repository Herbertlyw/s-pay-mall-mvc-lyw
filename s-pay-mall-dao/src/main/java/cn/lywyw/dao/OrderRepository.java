package cn.lywyw.dao;

import cn.lywyw.domain.Order;
import cn.lywyw.domain.Product;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class OrderRepository {
    private static final RowMapper<Order> READ_ORDER = (row, index) -> new Order(
            row.getObject("order_id", UUID.class), row.getString("product_id"),
            row.getString("product_name"), row.getBigDecimal("total_amount"), row.getString("status"));
    private final JdbcTemplate jdbc;

    public OrderRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void insert(UUID id, String owner, UUID request, Product product) {
        jdbc.update("""
                INSERT INTO pay_order (order_id, user_id, request_id, product_id, product_name, total_amount, status)
                VALUES (?, ?, ?, ?, ?, ?, 'pay_wait')
                """, id, owner, request, product.id(), product.name(), product.price());
    }

    public Optional<Order> byRequest(String owner, UUID request) {
        return jdbc.query("SELECT * FROM pay_order WHERE user_id=? AND request_id=?", READ_ORDER, owner, request)
                .stream().findFirst();
    }

    public Optional<Order> byId(String owner, UUID id) {
        return jdbc.query("SELECT * FROM pay_order WHERE user_id=? AND order_id=?", READ_ORDER, owner, id)
                .stream().findFirst();
    }

    public void finish(String owner, UUID id, String status) {
        jdbc.update("""
                UPDATE pay_order SET status=?, pay_time=CASE WHEN ?='pay_success' THEN CURRENT_TIMESTAMP ELSE NULL END
                WHERE user_id=? AND order_id=? AND status='pay_wait'
                """, status, status, owner, id);
    }
}
