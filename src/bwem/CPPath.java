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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Type of all the Paths used in BWEM (Cf. BWMap::GetPath).
 *
 * <p>cp.h:68:typedef std::vector<const ChokePoint *> Path; cp.h:168:typedef ChokePoint::Path
 * CPPath;
 */
public final class CPPath implements Iterable<ChokePoint> {

    static final CPPath EMPTY_PATH = new CPPath();
    private final List<ChokePoint> chokepoints;

    CPPath() {
        this.chokepoints = new ArrayList<>();
    }

    public int size() {
        return this.chokepoints.size();
    }

    public ChokePoint get(final int index) {
        return this.chokepoints.get(index);
    }

    void add(final ChokePoint chokepoint) {
        this.chokepoints.add(chokepoint);
    }

    void add(final int index, final ChokePoint chokepoint) {
        this.chokepoints.add(index, chokepoint);
    }

    void clear() {
        this.chokepoints.clear();
    }

    public boolean isEmpty() {
        return this.chokepoints.isEmpty();
    }

    @Override
    public Iterator<ChokePoint> iterator() {
        return chokepoints.iterator();
    }
}
