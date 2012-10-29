package org.openantivirus.ole;

import java.util.*;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEPageList.java,v 1.1 2001/09/15 06:44:34 kurti Exp $
 */
public class OLEPageList {
  private List listPages = new ArrayList();
  
  public void addPageIndex( int iIndex ) {
    listPages.add( new Integer( iIndex ) );
  }
  
  public int size() {
    return listPages.size();
  }
  
  public int getPageIndex( int iIndex ) {
    return ( (Integer) listPages.get( iIndex ) ).intValue();
  }
  
  public Iterator iterator() {
    return listPages.iterator();
  }
}
