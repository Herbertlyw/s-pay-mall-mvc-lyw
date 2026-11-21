package cn.lywyw.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record Order(UUID id, String productId, String productName, BigDecimal totalAmount, String status) {}
