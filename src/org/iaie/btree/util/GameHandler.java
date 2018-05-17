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

package org.iaie.btree.util;

import bwem.BWEM;
import bwta.BWTA;
import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.Player;

/**
 * @author momartin
 */
public abstract class GameHandler {
    protected Player self;
    public BW bw;
    public InteractionHandler ih;
    public BWTA bwta;
    public BWEM bwem;

    public GameHandler(BW bw, BWTA bwta, BWEM bwem) {
        this.bw = bw;
        this.ih = bw.getInteractionHandler();
        this.self = bw.getInteractionHandler().self();
        this.bwta = bwta;
        this.bwem = bwem;
    }
}
