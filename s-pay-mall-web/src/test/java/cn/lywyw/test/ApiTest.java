package cn.lywyw.test;

import cn.lywyw.domain.Order;
import cn.lywyw.service.ShopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:shop;DB_CLOSE_DELAY=-1")
class ApiTest {
    @Autowired ShopService shop;

    @Test
    void retriesPayOnlyOnceAndDoNotExposeAnotherSession() {
        UUID request = UUID.randomUUID();
        var first = CompletableFuture.supplyAsync(() -> shop.create("buyer", ShopService.KEYBOARD.id(), request));
        Order order = shop.create("buyer", ShopService.KEYBOARD.id(), request);
        assertEquals(first.join().id(), order.id());
        assertEquals("pay_success", shop.finish("buyer", order.id(), true).status());
        assertEquals("pay_success", shop.finish("buyer", order.id(), true).status());
        assertThrows(IllegalStateException.class, () -> shop.finish("buyer", order.id(), false));
        assertThrows(NoSuchElementException.class, () -> shop.get("stranger", order.id()));
    }

    @Test
    void cancelledOrderCannotBePaid() {
        Order order = shop.create("buyer", ShopService.KEYBOARD.id(), UUID.randomUUID());
        assertEquals("close", shop.finish("buyer", order.id(), false).status());
        assertThrows(IllegalStateException.class, () -> shop.finish("buyer", order.id(), true));
    }
}
