package org.dimasik.liteauction.api.events;

public interface Cancellable {
    boolean isCancelled();
    void setCancelled(boolean cancel);
}