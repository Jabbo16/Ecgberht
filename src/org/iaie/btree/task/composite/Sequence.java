/************************************************************************
 * Planning and Learning Group PLG,
 * Department of Computer Science,
 * Carlos III de Madrid University, Madrid, Spain
 * http://plg.inf.uc3m.es
 *
 * Copyright 2017, Nerea Luis, Moises Martinez
 *
 * (Questions/bug reports now to be sent to Moises Martinez)
 *
 * This file is part of IAIE.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the IAIE API nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IAIE. If not, see <http://www.gnu.org/licenses/>.
 *
 ************************************************************************/

package org.iaie.btree.task.composite;

import org.iaie.btree.BehavioralTree;
import org.iaie.btree.task.Task;

/**
 * @param <GameHandler>
 * @author Moises Martinez <mmartinez at bebee.com>
 */
public class Sequence extends Composite {

    public Sequence(String name) {
        super(name, null);
    }

    public Sequence(String name, Task... tasks) {
        super(name, null);
        this.addChildren(tasks);
    }

    @Override
    public BehavioralTree.State run() {

        for (int i = 0; i < this.children.size(); i++) {
            BehavioralTree.State result = this.children.get(i).run();
            if (result == BehavioralTree.State.FAILURE) return BehavioralTree.State.FAILURE;
            if (result == BehavioralTree.State.ERROR) return BehavioralTree.State.ERROR;
        }
        return BehavioralTree.State.SUCCESS;
    }
}
