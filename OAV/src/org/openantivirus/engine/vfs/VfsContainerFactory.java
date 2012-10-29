/*
 * $Id: VfsContainerFactory.java,v 1.1 2003/12/14 11:08:26 kurti Exp $
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
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.engine.vfs;

import java.io.*;
import org.openantivirus.engine.*;

/**
 * Creates VfsContainers
 *
 * Pattern-Roles: Factory
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.1 $
 */
public interface VfsContainerFactory {
    /**
     * @return VfsContainer that handles this entry or null if this factory
     *         cannot handle this container
     */
    VfsContainer getContainer(VfsEntry entry, ScanConfiguration configuration)
    throws IOException;
}
