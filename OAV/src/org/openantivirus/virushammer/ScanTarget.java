/*
 * $Id: ScanTarget.java,v 1.4 2004/05/01 14:36:09 kurti Exp $
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

import java.io.*;

/**
 * A target to scan regulary
 *
 * Pattern-Roles: Container
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.4 $
 */
public class ScanTarget {
    public static final String VERSION =
        "$Id: ScanTarget.java,v 1.4 2004/05/01 14:36:09 kurti Exp $";
    
    /** Holds value of property path. */
    private File path;    
    
    /** Holds value of property subfolders. */
    private boolean subfolders;
    
    public ScanTarget() {
        // path and folder may be set later
    }
    
    public ScanTarget(File path, boolean subfolders) {
        this.path       = path;
        this.subfolders = subfolders;
    }
    
    /** Getter for property path.
     * @return Value of property path.
     */
    public File getPath() {
        return this.path;
    }
    
    /** Setter for property path.
     * @param path New value of property path.
     */
    public void setPath(File path) {
        this.path = path;
    }
    
    public String toString() {
        return path.getAbsolutePath();
    }
    
    /** Getter for property subfolders.
     * @return Value of property subfolders.
     */
    public boolean getSubfolders() {
        return this.subfolders;
    }
    
    /** Setter for property subfolders.
     * @param subfolders New value of property subfolders.
     */
    public void setSubfolders(boolean subfolders) {
        this.subfolders = subfolders;
    }
    
}
