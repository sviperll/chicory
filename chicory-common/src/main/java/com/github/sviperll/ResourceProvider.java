/*
 * Copyright 2014 Victor Nazarov <asviraspossible@gmail.com>.
 */
package com.github.sviperll;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
public class ResourceProvider<T> implements ResourceProviderDefinition<T> {
    public static <T> ResourceProvider<T> forExisting(final T resource) {
        return ResourceProvider.of(new ResourceProviderDefinition<T>() {
            @Override
            public void provideResourceTo(Consumer<? super T> consumer) {
                consumer.accept(resource);
            }
        });
    }
    public static <T> ResourceProvider<T> flatten(final ResourceProviderDefinition<? extends ResourceProviderDefinition<? extends T>> provider) {
        return ResourceProvider.of(new ResourceProviderDefinition<T>() {
            @Override
            public void provideResourceTo(final Consumer<? super T> consumer) {
                provider.provideResourceTo(new Consumer<ResourceProviderDefinition<? extends T>>() {
                    @Override
                    public void accept(ResourceProviderDefinition<? extends T> innerProvider) {
                        innerProvider.provideResourceTo(consumer);
                    }
                });
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> ResourceProvider<T> of(ResourceProviderDefinition<? extends T> source) {
        if (source instanceof ResourceProvider)
            return (ResourceProvider<T>)source;
        else
            return new ResourceProvider<>(source);
    }

    private final ResourceProviderDefinition<? extends T> source;
    private ResourceProvider(ResourceProviderDefinition<? extends T> source) {
        this.source = source;
    }

    @Override
    public void provideResourceTo(Consumer<? super T> consumer) {
        source.provideResourceTo(consumer);
    }

    public <U> ResourceProvider<U> map(final Applicable<? super T, U> function) {
        return ResourceProvider.of(new ResourceProviderDefinition<U>() {
            @Override
            public void provideResourceTo(final Consumer<? super U> consumer) {
                source.provideResourceTo(new Consumer<T>() {
                    @Override
                    public void accept(T value) {
                        consumer.accept(function.apply(value));
                    }
                });
            }
        });
    }
    public <U> ResourceProvider<U> flatMap(final Applicable<? super T, ? extends ResourceProviderDefinition<? extends U>> function) {
        return flatten(map(function));
    }
}
