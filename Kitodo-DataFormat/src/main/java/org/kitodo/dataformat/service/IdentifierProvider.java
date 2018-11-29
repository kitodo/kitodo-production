package org.kitodo.dataformat.service;

import java.util.Iterator;

class IdentifierProvider implements Iterator<String> {
    int i = 1;

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public String next() {
        return Integer.toString(i++);
    }

}
