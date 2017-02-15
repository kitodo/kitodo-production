/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * GPL3-License.txt file that was distributed with this source code.
 */
package org.goobi.production.plugin.CataloguePlugin.PicaPlugin;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The class provides the ability to iterate over the child elements of a DOM
 * element.
 *
 * @author Matthias Ronge
 */
class GetChildElements implements Iterable<Element>, Iterator<Element> {
    /**
     * The next child to offer
     */
    private Element next;

    /**
     * Creates a class to iterate over the children of a DOM element.
     *
     * @param parent
     *            parent over whose children shall be iterated
     */
    GetChildElements(Element parent) {
        next = filter(parent.getFirstChild());
    }

    /**
     * Returns the next element, if any, or {@code null}.
     *
     * @param node
     *            node that might be the next candidate
     * @return the next element, or {@code null}
     */
    private Element filter(Node node) {
        while (node != null && !(node instanceof Element)) {
            node = node.getNextSibling();
        }
        return (Element) node;
    }

    /**
     * Returns whether the iterator can return another element.
     *
     * @see java.util.Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
        return next != null;
    }

    /**
     * Returns an iterator instance, which is this implementation.
     */
    @Override
    public Iterator<Element> iterator() {
        return this;
    }

    /**
     * Returns the next element while iterating.
     */
    @Override
    public Element next() {
        if (next == null) {
            throw new NoSuchElementException();
        }
        Element current = next;
        next = filter(next.getNextSibling());
        return current;
    }

    /**
     * This iterator does not support removal.
     */
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
