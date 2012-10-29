/*
 * $Id: StatusModel.java,v 1.3 2003/12/14 20:24:58 kurti Exp $
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

import java.beans.*;

/**
 * StatusModel
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class StatusModel {
    public static final String VERSION =
        "$Id: StatusModel.java,v 1.3 2003/12/14 20:24:58 kurti Exp $";
    
    /** Scanner-status */
    public static final int
            IDLE     = 0,
            SCANNING = 1;
    
    /** Holds value of property text. */
    private String text;
    
    /** Utility field used by bound properties. */
    private PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport(this);
    
    /** Holds value of property scanning. */
    private int scanning;
    
    /** Adds a PropertyChangeListener to the listener list.
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(String property,
                                          PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(property, l);
    }
    
    /** Removes a PropertyChangeListener from the listener list.
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(String property,
                                          PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(property, l);
    }
    
    /** Getter for property text.
     * @return Value of property text.
     */
    public String getText() {
        return this.text;
    }
    
    /** Setter for property text.
     * @param text New value of property text.
     */
    public void setText(String text) {
        String oldText = this.text;
        this.text = text;
        propertyChangeSupport.firePropertyChange("text", oldText, text);
    }
    
    /** Getter for property scanning.
     * @return Value of property scanning.
     */
    public int getScanning() {
        return this.scanning;
    }
    
    /** Setter for property scanning.
     * @param scanning New value of property scanning.
     */
    public void setScanning(int scanning) {
        int oldScanning = this.scanning;
        this.scanning = scanning;
        propertyChangeSupport.firePropertyChange("scanning", new Integer(oldScanning), new Integer(scanning));
    }
}
