/*
 * NumberCompare.java - A number comparator for the statement tree
 *
 * Copyright (c) 1996 Chuck McManis, All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * CHUCK MCMANIS MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. CHUCK MCMANIS
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT
 * OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package cocoa.basic;

import cocoa.util.Comparator;

class NumberCompare implements Comparator {

    public int compare(Object a, Object b) {
        int result;
        int x = ((Integer) a).intValue();
        int y = ((Integer) b).intValue();

        if (x < y)
            return -1;
        else if (x > y)
            return 1;
        return 0;
    }

    public boolean equals(Object a, Object b) {
        return compare(a, b) == 0;
    }

    public boolean lessThan(Object a, Object b) {
        return compare(a, b) < 0;
    }

    public boolean lessEqual(Object a, Object b) {
        return compare(a, b) <= 0;
    }

    public boolean greaterThan(Object a, Object b) {
        return compare(a, b) > 0;
    }

    public boolean greaterEqual(Object a, Object b) {
        return compare(a, b) >= 0;
    }
}
