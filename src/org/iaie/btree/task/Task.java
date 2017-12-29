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

package org.iaie.btree.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.iaie.btree.state.State;
import org.iaie.btree.util.GameHandler;

/**
 *
 * @author Moises Martinez <momartinm at gmail.com>
 * @param <GameHandler> Controller object which is used to collect information and execute
 * actions.
 */
public abstract class Task {
    
    private final String name;
    protected final List<Task> children;
    protected GameHandler handler;
    protected State state = State.RUNNING;
    
    public Task(String name, GameHandler gh) {
        this.state = State.READY;
        this.name = name;
        this.children = new ArrayList<>();
        this.handler = gh;
    }
    
    public Task(String name, GameHandler gh, Task ... tasks) {
        this(name, gh);
        this.children.addAll(Arrays.asList(tasks));
    }
    
    public String getName() {
        return this.name;
    }
    
    public void addChild(Task task) {
        this.children.add(task);
    }
    
    public Task getChild (int i) {
        return this.children.get(i);
    }
    
    public final void running() {
    	this.state = State.RUNNING;
    }
    
    public final void success() {
    	this.state = State.SUCCESS;        
    }
    
    public final void failure() {
    	this.state = State.FAILURE;        
    }
    
    public abstract State run();
}
