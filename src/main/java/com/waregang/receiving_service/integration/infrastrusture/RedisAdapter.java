//package com.waregang.receiving_service.receiving_process.infrastructure;
//
//import com.waregang.receiving_service.receiving_process.infrastructure.dto.GoodsReceiptDto;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.redis.core.BoundHashOperations;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Repository;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@Repository
//@RequiredArgsConstructor
//public class RedisAdapter implements CachePort {
//
//    private final RedisTemplate<String, GoodsReceiptDto> redisTemplate;
//
//    private static final String KEY_PREFIX = "opened_receipts:wh:";
//
//    @Override
//    public void saveReceivingDetails(PutGoodsReceiptDto dto) {
//        String hashKey = KEY_PREFIX + dto.warehouseId();
//        String fieldKey = dto.receiptId().toString();
//
//        BoundHashOperations<String, String, GoodsReceiptDto> ops =
//                redisTemplate.boundHashOps(hashKey);
//
//        ops.put(fieldKey, GoodsReceiptDto.from(dto));
//        ops.expire(Duration.ofHours(12));
//    }
//
//
//    @Override
//    public List<GoodsReceiptDto> findOpenedReceipts(String warehouseId) {
//        String hashKey = KEY_PREFIX + warehouseId;
//
//        BoundHashOperations<String, String, GoodsReceiptDto> ops =
//                redisTemplate.boundHashOps(hashKey);
//
//        Map<String, GoodsReceiptDto> entries = ops.entries();
//
//        return new ArrayList<>(entries.values());
//    }
//
//    @Override
//    public Optional<GoodsReceiptDto> findReceivingDetails(String warehouseId, String receiptId) {
//        String hashKey = KEY_PREFIX + warehouseId;
//
//        BoundHashOperations<String, String, GoodsReceiptDto> ops =
//                redisTemplate.boundHashOps(hashKey);
//
//        GoodsReceiptDto value = ops.get(receiptId);
//
//        return Optional.ofNullable(value);
//    }
//}