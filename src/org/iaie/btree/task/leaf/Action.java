/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.iaie.btree.task.leaf;

import ecgberht.GameState;

/**
 * @param <GameHandler>
 * @author nluis momartin
 */
public abstract class Action extends Leaf {

    public Action(String name, GameState gh) {
        super(name, gh);
    }
}
