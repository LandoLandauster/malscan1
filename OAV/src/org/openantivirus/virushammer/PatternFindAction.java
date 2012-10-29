/*
 * $Id: PatternFindAction.java,v 1.5 2005/03/03 12:18:06 kurti Exp $
 *
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

import java.awt.event.*;

/**
 * PatternFindAction
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.5 $
 */
public class PatternFindAction extends PerformableAction {
    private static final long serialVersionUID = 1L;
    
    public PatternFindAction() {
        super(L10N.getString("Find_pattern"));
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
        // not implemented yet
    }
    
    /** @returns if this action should be enabled  */
    public boolean isPerformable() {
        return false; // not implemented yet
    }
    
}
