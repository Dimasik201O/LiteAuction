package org.dimasik.liteauction.backend.mysql.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.dimasik.liteauction.backend.enums.AuctionType;
import org.dimasik.liteauction.backend.enums.BidsSortingType;
import org.dimasik.liteauction.backend.enums.CategoryType;
import org.dimasik.liteauction.backend.enums.MarketSortingType;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuiData {
    private int id;
    private String player;
    private AuctionType auctionType;
    private CategoryType categoryType;
    private MarketSortingType marketSortingType;
    private BidsSortingType bidsSortingType;
    private Set<String> additionalFilters;
}