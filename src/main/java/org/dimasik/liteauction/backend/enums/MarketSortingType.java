package org.dimasik.liteauction.backend.enums;

public enum MarketSortingType {
    CHEAPEST_FIRST,
    EXPENSIVE_FIRST,
    CHEAPEST_PER_UNIT,
    EXPENSIVE_PER_UNIT,
    NEWEST_FIRST,
    OLDEST_FIRST;

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