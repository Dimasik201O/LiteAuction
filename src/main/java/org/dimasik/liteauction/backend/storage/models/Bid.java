package org.dimasik.liteauction.backend.storage.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Bid {
    private int id;
    private int itemId;
    private String player;
    private int price;
}