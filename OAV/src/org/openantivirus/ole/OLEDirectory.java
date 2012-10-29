package org.openantivirus.ole;

import java.util.*;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEDirectory.java,v 1.2 2004/05/01 14:36:09 kurti Exp $
 */
public class OLEDirectory {
  private List listDirectoryEntry = new ArrayList();
  
  public List getDirectoryEntryList() {
    return listDirectoryEntry;
  }
  
  public OLEDirectoryEntry getDirectoryEntry( int iIndex ) {
    return (OLEDirectoryEntry) listDirectoryEntry.get( iIndex );
  }
  
  public int size() {
    return listDirectoryEntry.size();
  }
  
  public void addDirectoryPage( OLEPage olePage ) {
    for( int i = 0; i < OLEPage.SIZE; i += OLEDirectoryEntry.SIZE ) {
      listDirectoryEntry.add( new OLEDirectoryEntry( olePage, i ) );
    }
  }
}
