package com.annam.shop.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class OrderCodes {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private OrderCodes() {
    }

    public static String next() {
        String day = LocalDate.now(ZONE).format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "ORD-" + day + "-" + suffix;
    }
}
