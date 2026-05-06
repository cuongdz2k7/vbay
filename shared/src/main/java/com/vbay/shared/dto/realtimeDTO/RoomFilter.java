package com.vbay.shared.dto.realtimeDTO;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.vbay.shared.enums.realtime.RoomFilterKey;

public final class RoomFilter {
    private final Map<String, String> filters;

    private RoomFilter(Map<String, String> filters) {
        this.filters = Map.copyOf(filters);
    }

    public static RoomFilter none() {
        return new RoomFilter(Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public String keyPart() {
        if (filters.isEmpty()) {
            return "all";
        }

        return filters.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .flatMap(e -> Stream.of(encode(e.getKey()), encode(e.getValue())))
            .collect(Collectors.joining(":"));
    }

    ///tránh các kí tự nguy hiểm & : làm hỏng key filter
    // các kí tự đặc biệt encode sang UTF_8 đều chuyển thành %XX
    private static String encode(String raw) {
        return URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }


    public static final class Builder {
        private final Map<String, String> map = new HashMap<>();

        public Builder put(RoomFilterKey key, Object value) {
            if (value != null) {
                map.put(key.key(), String.valueOf(value));
            }
            return this;
        }

        public RoomFilter build() {
            return new RoomFilter(map);
        }
    }
}
