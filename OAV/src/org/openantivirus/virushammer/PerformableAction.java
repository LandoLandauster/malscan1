/*
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
 * ***** END LICENSE BLOCK *****
 */

package org.openantivirus.virushammer;

import javax.swing.*;

/**
 * PropertyChangeAction
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Id: PerformableAction.java,v 1.3 2003/12/14 20:24:58 kurti Exp $
 */
public abstract class PerformableAction extends AbstractAction {
    public static final String VERSION =
        "$Id: PerformableAction.java,v 1.3 2003/12/14 20:24:58 kurti Exp $";
    
    public PerformableAction(String text) {
        super(text);
        updateEnabled();
    }
    
    /**
     * sets the action to enabled/disabled, according to the result of
     * isPerformable(); this method should be called after the internal state
     * of the action has changed in a way that can affect the performability
     */
    protected void updateEnabled() {
        setEnabled(isPerformable());
    }
    
    /** @returns if this action should be enabled */
    public abstract boolean isPerformable();
}
