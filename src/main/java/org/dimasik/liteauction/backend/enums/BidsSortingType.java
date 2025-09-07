package org.dimasik.liteauction.backend.enums;

public enum BidsSortingType {
    CHEAPEST_FIRST,
    EXPENSIVE_FIRST,
    NEWEST_FIRST,
    OLDEST_FIRST;

    public BidsSortingType relative(boolean next) {
        BidsSortingType[] values = BidsSortingType.values();
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
