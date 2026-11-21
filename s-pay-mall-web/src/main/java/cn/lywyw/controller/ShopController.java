package cn.lywyw.controller;

import cn.lywyw.domain.Order;
import cn.lywyw.domain.Product;
import cn.lywyw.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ShopController {
    private final ShopService shop;

    public ShopController(ShopService shop) {
        this.shop = shop;
    }

    public record Checkout(String productId, UUID requestId) {}

    @GetMapping("/product")
    public Product product() { return ShopService.KEYBOARD; }

    @PostMapping("/orders")
    public Order create(@RequestBody Checkout body, HttpSession session) {
        return shop.create(session.getId(), body.productId(), body.requestId());
    }

    @GetMapping("/orders/{id}")
    public Order get(@PathVariable UUID id, HttpSession session) { return shop.get(session.getId(), id); }

    @PostMapping("/orders/{id}/demo-pay")
    public Order pay(@PathVariable UUID id, HttpSession session) { return shop.finish(session.getId(), id, true); }

    @PostMapping("/orders/{id}/cancel")
    public Order cancel(@PathVariable UUID id, HttpSession session) { return shop.finish(session.getId(), id, false); }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(IllegalArgumentException e) { return Map.of("message", e.getMessage()); }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> missing(NoSuchElementException e) { return Map.of("message", e.getMessage()); }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> conflict(IllegalStateException e) { return Map.of("message", e.getMessage()); }
}
