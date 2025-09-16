package org.dimasik.liteauction.backend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarketSortingType {
    CHEAPEST_FIRST("Сначала дешевые"),
    EXPENSIVE_FIRST("Сначала дорогие"),
    CHEAPEST_PER_UNIT("Сначала дешевые за ед. товара"),
    EXPENSIVE_PER_UNIT("Сначала дорогие за ед. товара"),
    NEWEST_FIRST("Сначала новые"),
    OLDEST_FIRST("Сначала старые");

    private final String displayName;

    public MarketSortingType relative(boolean next) {
        MarketSortingType[] values = MarketSortingType.values();
        int currentOrdinal = this.ordinal();
        int nextOrdinal;

        if (next) {
            nextOrdinal = currentOrdinal + 1;
            if (nextOrdinal >= values.length) {
                nextOrdinal = 0;
            }
        } else {
            nextOrdinal = currentOrdinal - 1;
            if (nextOrdinal < 0) {
                nextOrdinal = values.length - 1;
            }
        }

        return values[nextOrdinal];
    }
}