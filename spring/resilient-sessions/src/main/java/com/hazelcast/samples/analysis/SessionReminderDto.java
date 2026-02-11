package com.hazelcast.samples.analysis;

import com.hazelcast.samples.model.BasketItem;

import java.math.BigDecimal;
import java.util.List;

record SessionReminderDto(String sessionId, String userId, String timeStatus, List<BasketItem> items,
                          BigDecimal totalPrice) {
}
