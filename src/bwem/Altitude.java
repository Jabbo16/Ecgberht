// Original work Copyright (c) 2015, 2017, Igor Dimitrijevic
// Modified work Copyright (c) 2017-2018 OpenBW Team

//////////////////////////////////////////////////////////////////////////
//
// This file is part of the BWEM Library.
// BWEM is free software, licensed under the MIT/X11 License.
// A copy of the license is provided with the library in the LICENSE file.
// Copyright (c) 2015, 2017, Igor Dimitrijevic
//
//////////////////////////////////////////////////////////////////////////

package bwem;

import bwem.util.Pair;

import java.util.Comparator;

/**
 * Immutable wrapper of the integer primitive to satisfy the original C++ definition:
 * defs.h:54:typedef int16_t altitude_t;
 *
 * <p>Type of the altitudes in pixels.
 */
public final class Altitude implements Comparable<Altitude> {
    public static final Altitude UNINITIALIZED = new Altitude(-1);
    public static final Altitude ZERO = new Altitude(0);
    public static final Comparator<Pair<?, Altitude>> BY_ALTITUDE_ORDER = Comparator.comparing(p -> p.getRight().intValue());
    private final int val;

    Altitude(final int val) {
        this.val = val;
    }

    public int intValue() {
        return this.val;
    }

    @Override
    public int compareTo(final Altitude that) {
        return Integer.compare(this.val, that.val);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        } else if (!(object instanceof Altitude)) {
            return false;
        } else {
            final Altitude that = (Altitude) object;
            return (this.val == that.val);
        }
    }

    @Override
    public int hashCode() {
        return this.val;
    }

    @Override
    public String toString() {
        return String.valueOf(this.val);
    }
}
