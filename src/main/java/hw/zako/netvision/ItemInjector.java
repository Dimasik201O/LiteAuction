package hw.zako.netvision;

import org.dimasik.liteauction.backend.storage.models.SellItem;

import java.util.List;
import java.util.Random;

public class ItemInjector {
    private static final double PRICE_MULTIPLIER = 0.1;
    private static final double INJECT_CHANCE = 0.75;

    public static int injectItem(List<SellItem> items, int page, int page_size){
        if(items.isEmpty()){
            return -2;
        }
        if(new Random().nextDouble() > INJECT_CHANCE){
            return -1;
        }

        int tempPageSize = page_size;
        int pages = items.size() / page_size + (items.size() % page_size == 0 ? 0 : 1);
        if(page == pages){
            tempPageSize = items.size() % page_size;
        }
        int random = new Random().nextInt(tempPageSize) + (page - 1) * page_size;
        random = Math.min(items.size() - 1, random);

        int randomItemIndex = new Random().nextInt(items.size());
        SellItem randomItem = items.get(randomItemIndex).clone();
        randomItem.setPrice((int) ((double) randomItem.getPrice() * PRICE_MULTIPLIER));
        randomItem.setAmount(new Random().nextInt(randomItem.getAmount()) + 1);
        randomItem.setFake(true);
        items.add(random, randomItem);
        return random % page_size;
    }
}
