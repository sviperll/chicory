/*
 * Copyright (c) 2015, vir
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation and/or
 *     other materials provided with the distribution.
 *
 *  3. Neither the name of the copyright holder nor the names of its contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 *  THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *   LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 *  EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.github.sviperll.collection;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 *
 * @author vir
 * @param <T> type of list element
 */
public final class BTreeList<T> extends AbstractList<T> {
    private static int indent = 0;

    private Node<T> root = null;
    private final int nodeSize;

    public BTreeList(int nodeSize) {
        this.nodeSize = nodeSize;
    }

    @Override
    public void add(int index, T value) {
        try {
            if (root == null) {
                LeafNode<T> newRoot = new LeafNode<T>();
                newRoot.values = new Object[nodeSize];
                newRoot.size = 0;
                root = newRoot;
            }
            boolean added = root.addWithoutSplit(index, value);
            if (!added) {
                IntermidiateNode<T> newRoot = new IntermidiateNode<T>();
                newRoot.children = new Node[nodeSize];
                newRoot.sizeSums = new int[nodeSize];
                newRoot.children[0] = root;
                newRoot.sizeSums[0] = root.size();
                newRoot.nSubNodes = 1;
                root = newRoot;
                root.addWithoutSplit(index, value);
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
    }

    @Override
    public int size() {
        return root == null ? 0 : root.size();
    }

    @Override
    public boolean remove(Object o) {
        return root == null ? false : root.removeValue(o);
    }

    @Override
    public void clear() {
        root = null;
    }

    @Override
    public T get(int index) {
        try {
            return root.get(index);
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
    }

    @Override
    public T set(int index, T element) {
        try {
            return root.set(index, element);
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
    }

    @Override
    public T remove(int index) {
        try {
            return root.removeAt(index);
        } catch (IndexOutOfBoundsException ex) {
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        if (root == null) {
            if (index != 0)
                throw new IndexOutOfBoundsException(Integer.toString(index));
            else
                return java.util.Collections.emptyListIterator();
        } else {
            try {
                return root.listIterator(this, index, index);
            } catch (IndexOutOfBoundsException ex) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
        }
    }

    String toDebugString() {
        return "BTreeList{" + "nodeSize=" + nodeSize + ", root=" + root + '}';
    }

    private static abstract class Node<T> {
        abstract boolean addWithoutSplit(int index, T value);
        abstract Node<T> splitOutNewNode();
        abstract int size();

        abstract boolean removeValue(Object o);
        abstract T get(int index);
        abstract T set(int index, T element);
        abstract T removeAt(int index);
        abstract ListIterator<T> listIterator(BTreeList<T> list, int globalIndex, int index);
    }

    private static class IntermidiateNode<T> extends Node<T> {
        Node<T>[] children;
        int nSubNodes;
        int[] sizeSums;

        private int getSubNodeIndexForElementIndexToInsert(int index) {
            if (index == 0)
                return 0;
            int leftNodeIndex = 0;
            int rightNodeIndex = nSubNodes;
            while (rightNodeIndex > leftNodeIndex) {
                int nodeIndex = leftNodeIndex + (rightNodeIndex - leftNodeIndex) / 2;
                int subNodeLocalIndex = localIndexForSubNode(index, nodeIndex);
                if (subNodeLocalIndex <= 0)
                    rightNodeIndex = nodeIndex;
                else if (subNodeLocalIndex > sizeSums[nodeIndex])
                    leftNodeIndex = nodeIndex + 1;
                else
                    return nodeIndex;
            }
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }

        private int getSubNodeIndexForElementIndex(int index) {
            int leftNodeIndex = 0;
            int rightNodeIndex = nSubNodes;
            while (rightNodeIndex > leftNodeIndex) {
                int nodeIndex = leftNodeIndex + (rightNodeIndex - leftNodeIndex) / 2;
                int subNodeLocalIndex = localIndexForSubNode(index, nodeIndex);
                if (subNodeLocalIndex < 0)
                    rightNodeIndex = nodeIndex;
                else if (subNodeLocalIndex >= sizeSums[nodeIndex])
                    leftNodeIndex = nodeIndex + 1;
                else
                    return nodeIndex;
            }
            throw new IndexOutOfBoundsException(Integer.toString(index));
        }

        private int localIndexForSubNode(int index, int subNodeIndex) {
            return subNodeIndex == 0 ? index : index - sizeSums[subNodeIndex - 1];
        }

        @Override
        boolean addWithoutSplit(int index, T value) {
            int subNodeIndex = getSubNodeIndexForElementIndexToInsert(index);
            Node<T> subNode = children[subNodeIndex];
            boolean added = subNode.addWithoutSplit(localIndexForSubNode(index, subNodeIndex), value);
            if (added) {
                for (int i = subNodeIndex; i < nSubNodes; i++) {
                    sizeSums[i]++;
                }
                return true;
            } else if (nSubNodes >= children.length) {
                return false;
            } else {
                Node<T> newSubNode = subNode.splitOutNewNode();
                int newSubNodeIndex = subNodeIndex + 1;
                for (int i = nSubNodes - 1; i >= newSubNodeIndex; i--) {
                    children[i + 1] = children[i];
                    sizeSums[i + 1] = sizeSums[i];
                }
                children[newSubNodeIndex] = newSubNode;
                sizeSums[subNodeIndex] = (subNodeIndex == 0 ? 0 : sizeSums[subNodeIndex - 1]) + subNode.size();
                sizeSums[newSubNodeIndex] = sizeSums[subNodeIndex] + newSubNode.size();
                nSubNodes++;

                if (sizeSums[newSubNodeIndex] <= index) {
                    subNode = newSubNode;
                    subNodeIndex = newSubNodeIndex;
                }
                added = subNode.addWithoutSplit(localIndexForSubNode(index, subNodeIndex), value);
                assert added;
                for (int i = subNodeIndex; i < nSubNodes; i++) {
                    sizeSums[i]++;
                }
                return true;
            }
        }

        @Override
        Node<T> splitOutNewNode() {
            IntermidiateNode<T> result = new IntermidiateNode<T>();
            int rangeStart = children.length / 2;
            result.children = Arrays.copyOfRange(children, rangeStart, rangeStart + children.length);
            result.nSubNodes = nSubNodes - rangeStart;
            result.sizeSums = Arrays.copyOfRange(sizeSums, rangeStart, rangeStart + children.length);
            for (int i = 0; i < result.nSubNodes; i++) {
                result.sizeSums[i] = result.sizeSums[i] - sizeSums[rangeStart - 1];
            }
            Arrays.fill(children, rangeStart, nSubNodes, null);
            Arrays.fill(sizeSums, rangeStart, nSubNodes, sizeSums[rangeStart - 1]);
            nSubNodes = rangeStart;
            return result;
        }

        @Override
        int size() {
            return nSubNodes == 0 ? 0 : sizeSums[nSubNodes - 1];
        }

        @Override
        boolean removeValue(Object o) {
            boolean result = false;
            int nSkip = 0;
            for (int i = 0; i < nSubNodes;) {
                if (nSkip > 0) {
                    children[i] = children[i + nSkip];
                }
                Node<T> node = children[i];
                if (node.removeValue(o)) {
                    result = true;
                }
                int size = node.size();
                if (size != 0) {
                    sizeSums[i] = (i == 0 ? 0 : sizeSums[i - 1]) + size;
                    i++;
                } else {
                    nSkip++;
                    nSubNodes--;
                }
            }
            for (int i = 0; i < nSkip; i++) {
                children[nSubNodes + i] = null;
                sizeSums[nSubNodes + i] = nSubNodes == 0 ? 0 : sizeSums[nSubNodes - 1];
            }
            return result;
        }

        @Override
        T get(int index) {
            int subNodeIndex = getSubNodeIndexForElementIndex(index);
            Node<T> subNode = children[subNodeIndex];
            return subNode.get(localIndexForSubNode(index, subNodeIndex));
        }

        @Override
        T set(int index, T element) {
            int subNodeIndex = getSubNodeIndexForElementIndex(index);
            Node<T> subNode = children[subNodeIndex];
            return subNode.set(localIndexForSubNode(index, subNodeIndex), element);
        }

        @Override
        T removeAt(int index) {
            int subNodeIndex = getSubNodeIndexForElementIndex(index);
            Node<T> subNode = children[subNodeIndex];
            T result = subNode.removeAt(localIndexForSubNode(index, subNodeIndex));
            for (int i = subNodeIndex; i < nSubNodes; i++)
                sizeSums[i]--;
            return result;
        }

        @Override
        ListIterator<T> listIterator(BTreeList<T> list, int globalIndex, int index) {
            int subNodeIndex = getSubNodeIndexForElementIndex(index);
            Node<T> subNode = children[subNodeIndex];
            return subNode.listIterator(list, globalIndex, localIndexForSubNode(index, subNodeIndex));
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append('\n');
            for (int i = 0; i < indent; i++)
                result.append(' ');

            result.append("IntermidiateNode{\n");
            indent += 4;
            try {
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                result.append("nSubNodes=").append(nSubNodes).append(",\n");
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                result.append("sizeSums=").append(Arrays.toString(sizeSums)).append(",\n");
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                indent += 4;
                try {
                    result.append("children=").append(Arrays.toString(children)).append("}");
                    return result.toString();
                } finally {
                    indent -= 4;
                }
            } finally {
                indent -= 4;
            }
        }
    }

    private static class LeafNode<T> extends Node<T> {
        Object[] values;
        LeafNode<T> next;
        LeafNode<T> previous;
        private int size;

        @Override
        boolean addWithoutSplit(int index, T value) {
            if (size >= values.length) {
                return false;
            } else {
                for (int i = size - 1; i >= index; i--) {
                    values[i + 1] = values[i];
                }
                values[index] = value;
                size++;
                return true;
            }
        }

        @Override
        Node<T> splitOutNewNode() {
            LeafNode<T> result = new LeafNode<T>();
            int rangeStart = values.length / 2;
            result.values = Arrays.copyOfRange(values, rangeStart, rangeStart + values.length);
            Arrays.fill(this.values, rangeStart, size, null);
            result.next = this.next;
            this.next = result;
            result.previous = this;
            result.size = this.size - rangeStart;
            this.size = rangeStart;
            return result;
        }

        @Override
        int size() {
            return size;
        }

        @Override
        boolean removeValue(Object o) {
            int nSkip = 0;
            for (int i = 0; i < size;) {
                values[i] = values[i + nSkip];
                if (!values[i].equals(o))
                    i++;
                else {
                    nSkip++;
                    size--;
                }
            }
            for (int i = 0; i < nSkip; i++)
                values[size + i] = null;
            return nSkip > 0;
        }

        @Override
        @SuppressWarnings("unchecked")
        T get(int index) {
            return (T)values[index];
        }

        @Override
        T set(int index, T element) {
            T result = get(index);
            values[index] = element;
            return result;
        }

        @Override
        T removeAt(int index) {
            T result = get(index);
            for (int i = index; i < size - 1;) {
                values[i] = values[i + 1];
            }
            values[size - 1] = null;
            size--;
            return result;
        }

        @Override
        ListIterator<T> listIterator(BTreeList<T> list, int globalIndex, int localIndex) {
            return new BTreeIterator<T>(list, globalIndex, this, localIndex);
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            result.append('\n');
            for (int i = 0; i < indent; i++)
                result.append(' ');
            result.append("LeafNode#").append(System.identityHashCode(this)).append("{\n");
            indent += 4;
            try {
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                result.append("next=").append(System.identityHashCode(next)).append(",\n");
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                result.append("previous=").append(System.identityHashCode(previous)).append(",\n");
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                result.append("size=").append(size).append(",\n");
                for (int i = 0; i < indent; i++)
                    result.append(' ');
                indent += 4;
                try {
                    result.append("values=").append(Arrays.toString(values)).append("}");
                    return result.toString();
                } finally {
                    indent -= 4;
                }
            } finally {
                indent -= 4;
            }
        }
    }

    private static class BTreeIterator<T> implements ListIterator<T> {
        private final BTreeList<T> list;
        private LeafNode<T> node;
        private int nextGlobalIndex;
        private int nextLocalIndex;
        private int currentLocalIndex = -1;
        private int currentGlobalIndex = -1;

        BTreeIterator(BTreeList<T> list, int nextGlobalIndex, LeafNode<T> node, int nextLocalIndex) {
            this.list = list;
            this.node = node;
            this.nextGlobalIndex = nextGlobalIndex;
            this.nextLocalIndex = nextLocalIndex;
        }

        @Override
        public boolean hasNext() {
            return nextLocalIndex < node.size || node.next != null;
        }

        @Override
        public T next() {
            while (node.next != null && nextLocalIndex >= node.size) {
                nextLocalIndex -= node.size;
                node = node.next;
            }
            if (nextLocalIndex >= node.size)
                throw new NoSuchElementException();
            currentLocalIndex = nextLocalIndex;
            currentGlobalIndex = nextGlobalIndex;
            T result = node.get(nextLocalIndex);
            nextLocalIndex++;
            nextGlobalIndex++;
            return result;
        }

        @Override
        public boolean hasPrevious() {
            return nextLocalIndex - 1 >= 0 || node.previous != null;
        }

        @Override
        public T previous() {
            while (node.previous != null && nextLocalIndex - 1 < 0) {
                node = node.previous;
                nextLocalIndex += node.size;
            }
            if (nextLocalIndex - 1 < 0)
                throw new NoSuchElementException();
            currentLocalIndex = nextLocalIndex - 1;
            currentGlobalIndex = nextGlobalIndex - 1;
            T result = node.get(nextLocalIndex - 1);
            nextLocalIndex--;
            nextGlobalIndex--;
            return result;
        }

        @Override
        public int nextIndex() {
            return nextGlobalIndex;
        }

        @Override
        public int previousIndex() {
            return nextGlobalIndex - 1;
        }

        @Override
        public void remove() {
            list.remove(currentGlobalIndex);
        }

        @Override
        public void set(T e) {
            node.set(currentLocalIndex, e);
        }

        @Override
        public void add(T e) {
            list.add(nextGlobalIndex, e);
        }
    }
}
