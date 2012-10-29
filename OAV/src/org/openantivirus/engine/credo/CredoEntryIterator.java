/*
 * $Id: CredoEntryIterator.java,v 1.2 2004/05/18 09:10:16 kurti Exp $
 * 
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OAV.
 *
 * The Initial Developer of the Original Code is Kurt Huwig <kurt@huwig.de>.
 * Portions created by the Initial Developer are Copyright (C) 2001-2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.engine.credo;

import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * CredoEntryIterator
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.2 $
 */
class CredoEntryIterator implements Iterator {
    
    private final JarInputStream jarInputStream;
    
    /**
     * the next element to return or null if there is none
     */
    private Object nextElement = UNINITIALIZED;
    
    /** this value is set as 'nextElement' if 'hasNext' has not been called */
    private static final Object UNINITIALIZED = new Object();
    
    public CredoEntryIterator(JarInputStream jarInputStream) {
        this.jarInputStream = jarInputStream;
    }
    
    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    public boolean hasNext() {
        if (nextElement == UNINITIALIZED) {
            setNext();
        }
        return nextElement != null;
    }    
    
    /**
     * Returns the next element in the interation.
     *
     * @return the next element in the iteration.
     * @exception NoSuchElementException iteration has no more elements.
     */
    public Object next() {
        if (nextElement == UNINITIALIZED) {
            setNext();
        }
        if (nextElement == null) {
            throw new NoSuchElementException();
        }
        
        final Object result = nextElement;
        nextElement = UNINITIALIZED;
        return result;
    }
    
    /**
     *
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @exception UnsupportedOperationException if the <tt>remove</tt>
     * 		  operation is not supported by this Iterator.
     *
     * @exception IllegalStateException if the <tt>next</tt> method has not
     * 		  yet been called, or the <tt>remove</tt> method has already
     * 		  been called after the last call to the <tt>next</tt>
     * 		  method.
     */
    public void remove() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * sets the next element to the next element of the Enumeration or null,
     * if there is none
     */
    private void setNext() {
        JarEntry jarEntry;
        do {
            try {
                jarEntry = jarInputStream.getNextJarEntry();
            } catch (IOException ioe) {
                // happens at EOF
                jarEntry = null;
            }
        } while (jarEntry != null
                 && (jarEntry.isDirectory()
                     || jarEntry.getName().startsWith("META-INF/")));
        nextElement = (jarEntry != null
                       ? new CredoEntry(jarInputStream, jarEntry) : null);
    }
}
