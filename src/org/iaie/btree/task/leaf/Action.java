/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iaie.btree.task.leaf;

import org.iaie.btree.util.GameHandler;

/**
 *
 * @author nluis momartin
 * @param <GameHandler>
 */
public abstract class Action extends Leaf {

    public Action(String name, GameHandler gh) {
        super(name, gh);
    }
}
