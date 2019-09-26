/*
 * Copyright 2019 by Heiko Schäfer <heiko@rangun.de>
 *
 *  This file is part of android-webvirus.
 *
 *  android-webvirus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  android-webvirus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with android-webvirus.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Last modified 25.09.19 06:44 by heiko
 */

package de.rangun.webvirus.model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

public final class BKTree<T> implements Iterable<T> {

    private final class Node<N> {

        private final T item;
        private Map<Integer, Node<N>> children = null;

        Node(T item) {
            this.item = item;
        }

        T item() {
            return item;
        }

        void AddChild(int key, T item) {

            if(children == null) children = new Hashtable<>();

            children.put(key, new Node<>(item));
        }

        Node<N> Get(int key) {
            return children.get(key);
        }

        List<Integer> Keys() {
            return children == null ? new ArrayList<>() :
                    new ArrayList<>(children.keySet());
        }

        boolean ContainsKey(int key) {
            return children != null && children.containsKey(key);
        }
    }

    private Node<T> _Root = null;
    private int size = 0;

    public BKTree() {}

    public void Add(T item) {

        if(_Root == null) {
            _Root = new Node<>(item);
            ++size;
            return;
        }

        Node<T> curNode = _Root;

        final String it = item.toString().toLowerCase();

        int dist = LevenshteinDistance(curNode.item().toString().toLowerCase(), it);

        while(curNode.ContainsKey(dist)) {

            if(dist == 0) return;

            curNode = curNode.Get(dist);

            dist = LevenshteinDistance(curNode.item().toString().toLowerCase(), it);
        }

        curNode.AddChild(dist, item);
        ++size;
    }

    public List<String> Search(String word, int d) {
        List<String> rtn = new ArrayList<>();
        RecursiveSearch(_Root, rtn, word.toLowerCase(), d);
        return rtn;
    }

    private void RecursiveSearch(Node<T> node, List<String> rtn, String word, int d) {

        final int curDist = LevenshteinDistance(node.item().toString().toLowerCase(), word);

        final int minDist = curDist - d;
        final int maxDist = curDist + d;

        if(curDist <= d) rtn.add(node.item().toString());

        for(int key: node.Keys()) {
            if(key >= minDist && key <= maxDist) {
                RecursiveSearch(node.Get(key), rtn, word, d);
            }
        }
    }

    private static int LevenshteinDistance(String first, String second) {

        if(first.length() == 0) return second.length();
        if(second.length() == 0) return first.length();

        final int lenFirst = first.length();
        final int lenSecond = second.length();

        final int[][] d = new int[lenFirst + 1][lenSecond + 1];

        for(int i = 0; i <= lenFirst; ++i) d[i][0] = i;

        for(int i = 0; i <= lenSecond; ++i) d[0][i] = i;

        for(int i = 1; i <= lenFirst; ++i) {

            for(int j = 1; j <= lenSecond; ++j) {

                int match = (first.charAt(i - 1) == second.charAt(j - 1)) ? 0 : 1;

                d[i][j] = min(min(d[i - 1][j] + 1, d[i][j - 1] + 1),
                        d[i - 1][j - 1] + match);
            }
        }

        return d[lenFirst][lenSecond];
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {

        final class _iterator implements Iterator<T> {

            final Iterator<T> iter;
            final List<T> items = new ArrayList<>(size);

            private _iterator() {
                items.add(_Root.item());
                nextNode(_Root.children);
                iter = items.iterator();
            }

            private void nextNode(Map<Integer, Node<T>> children) {

                if(children != null) {
                    for(Node<T> n: children.values()) {
                        items.add(n.item());
                        nextNode(n.children);
                    }
                }
            }

            @Override
            public boolean hasNext() {
                return iter.hasNext();
            }

            @Override
            public T next() {
                return iter.next();
            }
        }

        return new _iterator();
    }
}
