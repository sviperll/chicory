/*
 * Copyright 2013 Victor Nazarov <asviraspossible@gmail.com>.
 */
package org.github.sviperll.stream;

/**
 *
 * @author Victor Nazarov <asviraspossible@gmail.com>
 */
abstract class DrainerRequest {
    @SuppressWarnings("rawtypes")
    private static DrainerRequestFactory FACTORY = new DrainerRequestFactory();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static DrainerRequestVisitor<DrainerRequest> factory() {
        return FACTORY;
    }
    public static DrainerRequest fetch() {
        return FACTORY.fetch();
    }
    public static DrainerRequest close() {
        return FACTORY.close();
    }

    private DrainerRequest() {
    }

    public abstract <R> R accept(DrainerRequestVisitor<R> visitor);

    private static class DrainerRequestFactory implements DrainerRequestVisitor<DrainerRequest> {
        @Override
        public DrainerRequest fetch() {
            return new DrainerRequest() {
                @Override
                public <R> R accept(DrainerRequestVisitor<R> visitor) {
                    return visitor.fetch();
                }
            };
        }

        @Override
        public DrainerRequest close() {
            return new DrainerRequest() {
                @Override
                public <R> R accept(DrainerRequestVisitor<R> visitor) {
                    return visitor.close();
                }
            };
        }
    }
}
