/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class DrainerResponse<T> {
    @SuppressWarnings("rawtypes")
    private static final DrainerResponseFactory FACTORY = new DrainerResponseFactory();

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <T> DrainerResponseVisitor<T, DrainerResponse<T>> factory() {
        return (DrainerResponseVisitor<T, DrainerResponse<T>>)FACTORY;
    }

    public static <T> DrainerResponse<T> fetched(T value) {
        return DrainerResponse.<T>factory().fetched(value);
    }

    public static <T> DrainerResponse<T> closed() {
        return DrainerResponse.<T>factory().closed();
    }

    public static <T> DrainerResponse<T> error(RuntimeException exception) {
        return DrainerResponse.<T>factory().error(exception);
    }

    private DrainerResponse() {
    }

    public abstract <R> R accept(DrainerResponseVisitor<T, R> visitor);

    private static class DrainerResponseFactory<T> implements DrainerResponseVisitor<T, DrainerResponse<T>> {
        @Override
        public DrainerResponse<T> fetched(final T value) {
            return new DrainerResponse<T>() {
                @Override
                public <R> R accept(DrainerResponseVisitor<T, R> visitor) {
                    return visitor.fetched(value);
                }
            };
        }

        @Override
        public DrainerResponse<T> closed() {
            return new DrainerResponse<T>() {
                @Override
                public <R> R accept(DrainerResponseVisitor<T, R> visitor) {
                    return visitor.closed();
                }
            };
        }

        @Override
        public DrainerResponse<T> error(final RuntimeException exception) {
            return new DrainerResponse<T>() {
                @Override
                public <R> R accept(DrainerResponseVisitor<T, R> visitor) {
                    return visitor.error(exception);
                }
            };
        }
    }
}
