/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ResourceSource<T> implements SourceableResource<T> {
    public static <T> ResourceSource<T> of(SourceableResource<T> source) {
        if (source instanceof ResourceSource)
            return (ResourceSource<T>)source;
        else
            return new ResourceSource<>(source);
    }

    private final SourceableResource<T> source;
    private ResourceSource(SourceableResource<T> source) {
        this.source = source;
    }

    @Override
    public BindedConsumer bindConsumer(Consumer<? super T> consumer) {
        return source.bindConsumer(consumer);
    }

    public <U> ResourceSource<U> map(final Applicable<? super T, U> function) {
        return ResourceSource.of(new SourceableResource<U>() {
            @Override
            public BindedConsumer bindConsumer(final Consumer<? super U> consumer) {
                return source.bindConsumer(new Consumer<T>() {
                    @Override
                    public void accept(T value) {
                        consumer.accept(function.apply(value));
                    }
                });
            }
        });
    }
}
