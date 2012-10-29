/*
 * $Id: L10N.java,v 1.3 2003/12/14 20:24:58 kurti Exp $
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

import java.util.*;

/**
 * L10N support
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.3 $
 */
public class L10N {
    public static final String VERSION =
        "$Id: L10N.java,v 1.3 2003/12/14 20:24:58 kurti Exp $";
    
    private static ResourceBundle bundle;
    
    /** sets the bundle to be used */
    public static void setResourceBundle(ResourceBundle bundle) {
        L10N.bundle = bundle;
    }
    
    /** @returns the localized string for this key or an error message */
    public static String getString(String name) {
        try {
            return bundle.getString(name);
        } catch (MissingResourceException mre) {
            final String errorMsg =
                    "*** Missing translation for '" + name + "' ***";
            mre.printStackTrace();
            return errorMsg;
        }
    }
    
    /**
     * @returns the Mnemonic-character for the appropriate String, either from
     *          the ResourceBundle or the first character of the String
     */
    public static char getMnemonic(String name) {
        try {
            return bundle.getString(name + "Mnemonic").charAt(0);
        } catch (MissingResourceException mre) {
            return getString(name).charAt(0);
        }
    }
    
}
