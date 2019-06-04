package com.fx360t.util;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface ConsumableFunction<T,R> extends Function<T,R> {
    default Consumer<T> thenAccept(Consumer<R> after){
        Objects.requireNonNull(after);
        return (T t) ->{after.accept(apply(t));};
    };

    default <V> ConsumableFunction<T, V> andThen(ConsumableFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }
}
