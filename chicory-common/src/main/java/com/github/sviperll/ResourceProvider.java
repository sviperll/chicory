/*
 * Copyright (c) 2014, Victor Nazarov <asviraspossible@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Victor Nazarov nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
