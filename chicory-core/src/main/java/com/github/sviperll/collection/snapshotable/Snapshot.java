

package com.github.sviperll.collection.snapshotable;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 *
 * @author Victor Nazarov &lt;asviraspossible@gmail.com&gt;
 */
@ParametersAreNonnullByDefault
public class Snapshot {
    public static <T> List<? extends T> unmodifiableListSnapshotOf(List<? extends T> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    public static <K, V> Map<? extends K, ? extends V> unmodifiableMapSnapshotOf(Map<? extends K, ? extends V> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    public static <T> Set<? extends T> unmodifiableSetSnapshotOf(Set<? extends T> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    public static <T> Collection<? extends T> unmodifiableCollectionSnapshotOf(Collection<? extends T> argument) {
        return preciseTypeSnapshotOf(argument);
    }

    private static <T> Collection<? extends T> preciseTypeSnapshotOf(Collection<T> argument) {
        if (UnsafeReferenceOwnership.noModifiableReferencesExistsAnywhere(argument)) {
            return argument;
        } else if (argument instanceof List) {
            return Snapshot.unmodifiableListSnapshotOf((List<T>)argument);
        } else if (argument instanceof Set) {
            return Snapshot.unmodifiableSetSnapshotOf((Set<T>)argument);
        } else {
            return UnsafeReferenceOwnership.unmodifiableWrapperForListWithNoOtherReferencesAnywhere(
                    Collections.unmodifiableList(new ArrayList<>(argument)));
        }
    }

    private static <T> Set<? extends T> preciseTypeSnapshotOf(Set<T> argument) {
        if (UnsafeReferenceOwnership.noModifiableReferencesExistsAnywhere(argument)) {
            return argument;
        } else if (argument instanceof SortedSet) {
            return UnsafeReferenceOwnership.unmodifiableWrapperForSetWithNoOtherReferencesAnywhere(
                    Collections.unmodifiableSet(new TreeSet<>(argument)));
        } else {
            return UnsafeReferenceOwnership.unmodifiableWrapperForSetWithNoOtherReferencesAnywhere(
                    Collections.unmodifiableSet(new HashSet<>(argument)));
        }
    }

    private static <T> List<? extends T> preciseTypeSnapshotOf(List<T> argument) {
        if (UnsafeReferenceOwnership.noModifiableReferencesExistsAnywhere(argument)) {
            return argument;
        } else if (argument instanceof SnapshotableList) {
            SnapshotableList<T> snapshotable = (SnapshotableList<T>)argument;
            return snapshotable.snapshot();
        } else {
            return UnsafeReferenceOwnership.unmodifiableWrapperForListWithNoOtherReferencesAnywhere(
                    new ArrayList<>(argument));
        }
    }

    private static <K, V> Map<? extends K, ? extends V> preciseTypeSnapshotOf(Map<K, V> argument) {
        if (UnsafeReferenceOwnership.noModifiableReferencesExistsAnywhere(argument)) {
            return argument;
        } else if (argument instanceof SnapshotableMap) {
            SnapshotableMap<K, V> snapshotable = (SnapshotableMap<K, V>)argument;
            return snapshotable.snapshot();
        } else if (argument instanceof SortedMap) {
            return UnsafeReferenceOwnership.unmodifiableWrapperForMapWithNoOtherReferencesAnywhere(
                    new TreeMap<>(argument));
        } else {
            return UnsafeReferenceOwnership.unmodifiableWrapperForMapWithNoOtherReferencesAnywhere(
                    new HashMap<>(argument));
        }
    }

    private Snapshot() {
    }
}
