/**********************************
 Copyright (c) All Rights Reserved
 *********************************/

package me.aj4real.simplepackets;

import java.util.*;
import java.util.function.Consumer;

public class ProxyList<T> implements List<T>, RandomAccess, Cloneable, java.io.Serializable {
    private final List<T> delegate;
    private final Consumer<T> add, remove;
    public ProxyList(List<T> delegate, Consumer<T> add, Consumer<T> remove) {
        this.delegate = delegate;
        this.add = add;
        this.remove = remove;
        for (T t : delegate) {
            patch(t);
        }
    }

    public void patch(T... connections) {
        for (T connection : connections) {
            add.accept(connection);
        }
    }

    public void clean(T... connections) {
        for (T connection : connections) {
            remove.accept(connection);
        }
    }

    @Override
    public boolean add(T connection) {
        patch(connection);
        return delegate.add(connection);
    }

    @Override
    public void add(int index, T element) {
        patch(element);
        delegate.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        for (T t : c) {
            patch(t);
        }
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        for (T t : c) {
            patch(t);
        }
        return delegate.addAll(index, c);
    }

    @Override
    public boolean remove(Object o) {
        clean((T) o);
        return delegate.remove(o);
    }

    @Override
    public T remove(int index) {
        clean(delegate.get(index));
        return delegate.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object o : c) {
            clean((T) o);
        }
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public T set(int index, T element) {
        try {
            T current = get(index);
            if(current != null) clean(current);
            patch(element);
        } catch (ArrayIndexOutOfBoundsException e) {}
        return delegate.set(index, element);
    }

    @Override
    public void clear() {
        for (T t : delegate) {
            clean(t);
        }
        delegate.clear();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return delegate.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public T get(int index) {
        return delegate.get(index);
    }

    @Override
    public int indexOf(Object o) {
        return delegate.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return delegate.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return delegate.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return delegate.subList(fromIndex, toIndex);
    }
}
