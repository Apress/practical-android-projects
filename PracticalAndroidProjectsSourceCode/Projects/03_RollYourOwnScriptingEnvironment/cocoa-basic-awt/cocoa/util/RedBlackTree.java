/*
 * @(#)RedBlackTree.java	1.1 95/09/14
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package cocoa.util;

import java.io.PrintStream;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * This class implements a Dictionary using a B-tree. This exact type
 * of tree is called a "red-black" tree since it uses a color algorithim
 * to insure that it is always balanced. Note that the key object must
 * be an object of class String for this class. This is because the
 * implementation uses compareTo() to determine if a node should be on
 * the right or left hand branch.
 *
 * @author	Chuck McManis
 * @version	1.1, 14 Sep 1995
 * @see		Dictionary
 */
public class RedBlackTree {
    private RedBlackTreeNode listRoot;
    private final int RED = 0;
    private final int BLACK = 1;
    private int count = 0;
    private long treeGen = 0; // This tree's generation count.

    Comparator cmp;

    public RedBlackTree(Comparator c) {
        cmp = c;
    }

    public RedBlackTree() {
        cmp = new StringCompare();
    }

    /**
     * Returns the number of elements in the tree.
     */
    public int size() {
    	return (count);
    }

    /**
     * Returns true if the tree is empty.
     */
    public boolean isEmpty() {
	    return (listRoot == null);
    }

    /**
     * Return an enumeration of the trees keys. This will return the
     * keys in sorted order as it does an inorder walk from the minimum
     * valued key to the maximum valued key.
     */
    public Enumeration keys() {
	    return new RedBlackTreeEnumerator(this, true);
    }

    /**
     * Return an enumeration of the trees objects. This will return the
     * elements in sorted order as it does an inorder walk from minimum
     * key to the  maximum key.
     */
    public Enumeration elements() {
	    return new RedBlackTreeEnumerator(this, false);
    }


    /**
     * Return the value for a given key.
     */
    public Object get(Object key) {
  	    RedBlackTreeNode x = lookup(key);
   	    return ((x != null) ? x.value : null);
    }

    /**
     * Add a new object to the tree. Note that if there is already a
     * node in the tree with this key value, the old value is replaced
     * with the new value. The old value is returned. If no previous
     * node exists, this returns null.
     */
    public Object put(Object key, Object value) {
    	RedBlackTreeNode x = add(key, value);
    	if (x == null)
    	    count++;	// Added a new element
    	return ((x != null) ? x.value : null);
    }

    /**
     * Remove an object from the tree. This returns null if the key did
     * not reference an object in the tree.
     */
    public Object remove(Object key) {
	    RedBlackTreeNode x = lookup(key);
	    if (x != null) {
		    delete(x);
		    count--;
		    return (x.value);
	    }
    	return (null); // throw something?
    }

    /**
     * Return the successor object to the named object.
     */
    public Object next(Object key) {
        RedBlackTreeNode x = lookup(key);
        if (x != null) {
            x = successor(x);
            if (x != null) {
                return (x.value);
            }
        }
        return null;
    }

    /**
     * Return the predecessor value to the named object.
     */
    public Object prev(Object key) {
        RedBlackTreeNode x = lookup(key);
        if (x != null) {
            x = predecessor(x);
            if (x != null) {
                return (x.value);
            }
        }
        return null;
    }

    /* ******************************************* *
     * PRIVATE Implementation of Red-Black B-trees *
     * ******************************************* */

    /**
     * Return the value of the generation variable
     */
    long generation() {
    	return treeGen;
    }

    /**
     * Rotate the tree left about node 'x'.
     */
    private void rotateLeft(RedBlackTreeNode x) {
    	RedBlackTreeNode y = x.right;

    	x.right = y.left;
    	if (y.left != null)
    	    y.left.parent = x;

    	y.parent = x.parent;
    	if (x.parent == null)
    		listRoot = y;
    	else {
    	    if (x == x.parent.left)
    	        x.parent.left = y;
      	    else
    	        x.parent.right = y;
    	}
    	y.left = x;
    	x.parent = y;
    }

    /**
     * Return the color of node x, note if node x is null then
     * the color is presumed to be black.
     */
    private int color(RedBlackTreeNode x) {
    	if (x == null)
    	    return BLACK;
    	else
    	    return (x.color);
    }

    /**
     * Rotate the tree right about the node y. Rotations preserve
     * the key ordering of their children nodes.
     */
    private void rotateRight(RedBlackTreeNode y) {
    	RedBlackTreeNode x = y.left;

    	y.left = x.right;

    	if (x.right != null)
    	    x.right.parent = y;

    	x.parent = y.parent;

    	if (y.parent == null)
    	    listRoot = x;
    	else {
    	    if (y == y.parent.left)
    	        y.parent.left = x;
    	    else
    	        y.parent.right = x;
    	}

    	x.right = y;
    	y.parent = x;
    }

    /**
     * Simple node insertion. After insertion the red-black properties
     * are "fixed" by rotating the correct number of nodes to balance
     * the tree. See add below.
     */
    private RedBlackTreeNode insert(RedBlackTreeNode z) {
    	RedBlackTreeNode x = listRoot;
    	RedBlackTreeNode y = null;
    	int	i;

    	while (x != null) {
    	    y = x;
    	    x = (cmp.compare(x.key, z.key) > 0) ? x.left : x.right;
    	}
    	z.parent = y;
    	if (y == null) {
    	    listRoot = z;
    	} else {
    	    i = cmp.compare(y.key, z.key);
    	    if (i > 0)
    	        y.left = z;
    	    else if (i < 0)
        		y.right = z;
    	    else { // They have the same key
        		Object t;

        		t = z.value;		// swap values
        		z.value = y.value;
        		y.value = t;
        		return (z);		// return old value
    	    }
    	}
    	return (null);
    }

    /**
     * Return the lowest key valued node. (needed for enumerations)
     */
    RedBlackTreeNode minimum() {
    	return minimum(listRoot);
    }

    /**
     * Return the lowest key value in the subtree under x
     */
    private RedBlackTreeNode minimum(RedBlackTreeNode x) {
    	if (x == null)
    	    x = listRoot;

    	while (x != null) {
    	    if (x.left == null)
    		return x;
    	    x = x.left;
     	}
    	return null;
    }

    /**
     * Return the highest value key under the subtree x
     */
    private RedBlackTreeNode maximum(RedBlackTreeNode x) {
    	if (x == null)
    	    x = listRoot;

    	while (x != null) {
    	    if (x.right == null)
    		return x;
    	    x = x.right;
    	}
    	return null;
    }


    /**
     *  Return the next higher key value following x.
     */
    RedBlackTreeNode successor(RedBlackTreeNode x) {
    	if (x.right != null)
    	    return (minimum(x.right));
    	RedBlackTreeNode y = x.parent;
    	while ((y != null) && (x == y.right)) {
    	    x = y;
    	    y = x.parent;
    	}
    	return y;
    }

    /**
     *  Return the next lower node to node x in key order
     */
    private RedBlackTreeNode predecessor(RedBlackTreeNode x) {
    	if (x.left != null)
    	    return (maximum(x.left));
    	RedBlackTreeNode y = x.parent;
    	while ((y != null) && (x == y.left)) {
    	    x = y;
    	    y = x.parent;
    	}
    	return y;
    }

    /**
     * Add a new node to the B-tree. Do the insertion, followed by
     * balancing. If the node is simply a replacement, skip the
     * balancing section.
     */
    private synchronized RedBlackTreeNode add(Object key, Object datum) {
    	RedBlackTreeNode x = new RedBlackTreeNode(key, datum);
    	RedBlackTreeNode y;
    	RedBlackTreeNode oldValue = null;

    	oldValue = insert(x);
    	if (oldValue != null) {
    	    return (oldValue);
    	}
    	treeGen++;	// note that it is a new tree

    	x.color = RED;

    	/* now fix up the tree */
    	while ((x != listRoot) && (x.parent.color == RED)) {

    	    if (x.parent == x.parent.parent.left) {
        		y = x.parent.parent.right;
        		if ((y != null) && (y.color == RED)) {
        		    x.parent.color = BLACK;
        		    y.color = BLACK;
        		    x.parent.parent.color = RED;
        		    x = x.parent.parent;
        		} else {
        		    if (x == x.parent.right) {
            			x = x.parent;
            			rotateLeft(x);
        		    }
        		    x.parent.color = BLACK;
        		    x.parent.parent.color = RED;
        		    rotateRight(x.parent.parent);
        		}
    	    } else {
        		y = x.parent.parent.left;
        		if ((y != null) && (y.color == RED)) {
        		    x.parent.color = BLACK;
        		    y.color = BLACK;
        		    x.parent.parent.color = RED;
        		    x = x.parent.parent;
        		} else {
        		    if (x == x.parent.left) {
            			x = x.parent;
            			rotateRight(x);
        		    }
        		    x.parent.color = BLACK;
        		    x.parent.parent.color = RED;
        		    rotateLeft(x.parent.parent);
        		}
    	    }
    	}
    	listRoot.color = BLACK;
    	return null;
    }

    /**
     * Return the node indexed by key.
     */
    private synchronized RedBlackTreeNode lookup(Object key) {
    	RedBlackTreeNode x = listRoot;
    	int	ci;

    	while (x != null) {
    	    ci = cmp.compare(x.key, key);
    	    if (ci < 0)
    		x = x.right;
    	    else if (ci > 0)
    		x = x.left;
    	    else
    		return x;
    	}
    	return (null);
    }

    /**
     * return a string of length 'n' blanks  (helper for printNode)
     */
    private String blanks(int n) {
    	StringBuffer z = new StringBuffer(n);
    	for (int q = 0; q < n; q++)
    	    z.append(" ");
    	return (z.toString());
    }

    String keyString(Object x) {
        return x.toString();
    }

    /**
     * Print a nice graphical representation of the tree on the
     * PrintStream passed. See printTree for the output.
     */
    private void printNode(RedBlackTreeNode x, String indent, PrintStream out) {

    	if (x == null)
    	    return;

    	if (x.right != null) {
    	    if ((x.parent != null) && (x == x.parent.left))
    	        printNode(x.right, indent+"|"+blanks(x.key.toString().length()+2), out);
    	    else
    	        printNode(x.right, indent+blanks(x.key.toString().length()+3), out);
    	}

    	out.print(indent);
    	out.print((x.color == BLACK) ? "@ " : "O ");
    	out.print(x.key);
    	if ((x.right != null) || (x.left != null))
    	    out.println(" +");
    	else
    	    out.println();

    	if (x.left == null) {
    	    if (x.parent != null) {
    		if (x == x.parent.right)
    	          indent = indent + "|";
    		else
    	          indent = indent + " ";
    	    }
    	    out.println(indent);
    	} else {
    	    if ((x.parent != null) && (x == x.parent.right))
    		indent = indent + "|" + blanks(x.key.toString().length()+2);
    	    else
    		indent = indent + " " + blanks(x.key.toString().length()+2);

    	    out.println(indent+"|");
    	    printNode(x.left, indent, out);
    	}
    }

    /**
     * Print a graphical version of the tree (ignores the 80 column limit)
     * on terminals. This function walks the tree and outputs a graphical
     * form with the keys in place. The format is as follows:
     * <pre>
     *                O fraz
     *                |
     *          @ foo +
     *          |
     * @ bletch +
     *          |
     *          |     O baz
     *          |     |
     *          @ bar +
     *</pre>
     * Each node is represented by '@' if it is black, and 'O' if it is
     * red, followed by its key, and its children. Nodes to the right in
     * output are lower in the tree than nodes to the left, the root node
     * is in column 1. Nodes lower on the output are to the "left" of
     * nodes earlier or higher in the output. (rotate 90 degrees clockwise)
     */
    public void printTree(PrintStream o) {
    	printNode(listRoot, "", o);
    }

    /**
     * Print the tree to System.out
     */
    public void printTree() {
	    printNode(listRoot, "", System.out);
    }

    /**
     * Fix up the coloring of the tree after a remove operation.
     */
    private void delete_fixup(RedBlackTreeNode x) {
    	RedBlackTreeNode w;

    	if (x.parent == null) // deleted last node in tree
    	    return;

    	while ((x != listRoot) && (x.color == BLACK)) {
    	    if (((x == nilNode) && (x.parent.left == null)) ||
    		(x == x.parent.left)) {
        		w = x.parent.right;
        		if (color(w) == RED) {
        		    w.color = BLACK;
        		    w.parent.color = RED;
        		    rotateLeft(x.parent);
        		    w = x.parent.right;
        		}
        		if ((color(w.left) == BLACK) && (color(w.right) == BLACK)) {
        		    w.color = RED;
        		    x = x.parent;
        		} else {
        		    if (color(w.right) == BLACK) {
        		    	w.left.color = BLACK;
        		    	w.color = RED;
        		    	rotateRight(w);
        		    	w = x.parent.right;
        		    }
        		    w.color = x.parent.color;
        		    x.parent.color = BLACK;
        		    w.right.color = BLACK;
        		    rotateLeft(x.parent);
        		    x = listRoot;
        		}
    	    } else {
        		w = x.parent.left;
        		if (color(w) == RED) {
        		    w.color = BLACK;
        		    w.parent.color = RED;
        		    rotateRight(x.parent);
        		    w = x.parent.left;
        		}
        		if ((color(w.left) == BLACK) && (color(w.right) == BLACK)) {
        		    w.color = RED;
        		    x = x.parent;
        		} else {
        		    if (color(w.left) == BLACK) {
        		    	w.right.color = BLACK;
        		    	w.color = RED;
        		    	rotateLeft(w);
        		    	w = x.parent.left;
        		    }
        		    w.color = x.parent.color;
        		    x.parent.color = BLACK;
        		    w.left.color = BLACK;
        		    rotateRight(x.parent);
        		    x = listRoot;
        		}
    	    }
    	}
    	x.color = BLACK;
    }

    // makes the delete algorithm cleaner
    private RedBlackTreeNode nilNode = new RedBlackTreeNode("Nil", "Nil");

    /**
     * Remove a node from the tree and if it is black, fix up the
     * tree so that the black height remains balanced.
     */
    private synchronized RedBlackTreeNode delete(RedBlackTreeNode z) {
    	RedBlackTreeNode x = null, y = null;

    	nilNode.color = BLACK;
    	treeGen++; // tree has changed, flag enumerators
    	if ((z.left == null) || (z.right == null))
    	    y = z;		// Z has only one child
    	else
    	    y = successor(z);

    	if (y.left != null)
    	    x = y.left;
    	else
    	    x = (y.right == null) ? nilNode : y.right;

    	x.parent = y.parent;

    	if (y.parent == null)
    	    listRoot = (x == nilNode) ? null : x;
    	else {
    	    if (y == y.parent.left)
        		y.parent.left = (x == nilNode) ? null : x;
    	    else
        		y.parent.right = (x == nilNode) ? null : x;
    	}
    	if (y != z) {
    	    z.key = y.key;
    	    z.value = y.value;
    	}

    	if (y.color == BLACK)
    	    delete_fixup(x);

    	return (y);
    }

    public String toString() {
	    return ("RedBlackTree object with "+count+" elements.");
    }
}

/**
 * This class defines an individual node on the B-tree itself. It is
 * private to btrees and has no methods.
 */
class RedBlackTreeNode {
    int color;
    Object value;
    Object key;

    RedBlackTreeNode right, left, parent;

    public RedBlackTreeNode(Object nodeKey, Object nodeValue) {
	    super();
	    key = nodeKey;
	    value = nodeValue;
    }

    public String toString() {
	    return ("K("+((color == 0)?"R":"B")+"):"+key);
    }
}

/**
 * This is the enumerator class. It implements the enumeration interface
 * and is very straightforward.
 *
 * Note that there is a race condition whereby an enumerator becomes
 * "invalid" should the tree be changed while it is held.
 */
class RedBlackTreeEnumerator implements Enumeration {
    // state for the enumerator
    private boolean keys;
    private RedBlackTree tree;
    private RedBlackTreeNode x;
    private long generation;

    /**
     * Create a new enumerator.
     */
    RedBlackTreeEnumerator(RedBlackTree tree, boolean keys) {
    	this.tree = tree;
    	this.keys = keys;
    	this.x = tree.minimum();
    	this.generation = tree.generation();
    }

    /**
     * Returns true if there is another element in the list.
     */
    public boolean hasMoreElements() {
    	return (x != null);
    }

    /**
     * Returns the next element.
     * @exception NoSuchElementException thrown if it is out of elements.
     * @exception Exception thrown if the tree has been modified.
     */
    public Object nextElement() {
    	RedBlackTreeNode y = x;

    	if (x == null)
    	    throw new NoSuchElementException("RedBlackTreeEnumerator");
    	if (generation != tree.generation())
    	    throw new NoSuchElementException(
    			"RedBlackTreeEnumerator: Tree modified during enumeration");

    	x = tree.successor(x);
    	return ((keys) ? y.key : y.value);
    }
}

