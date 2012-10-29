package org.openantivirus.ole;

import java.util.*;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEPageMap.java,v 1.2 2004/05/01 14:36:09 kurti Exp $
 */
public class OLEPageMap {
  public final static int
    SPECIAL_BLOCK = -3,
    END_OF_CHAIN  = -2,
    UNUSED        = -1;
  
  private List listPageMap = new ArrayList();
  
  public void addBlockPage( OLEPage olePage ) {
    listPageMap.add( olePage );
  }
  
  private int getNextPage( int iPageNr ) {
    return ( (OLEPage)listPageMap.get( iPageNr / ( OLEPage.SIZE / 4 ) ) ).
           getInt( ( iPageNr % ( OLEPage.SIZE / 4 ) ) * 4 );
  }
  
  public OLEPageList getPageList( int iStartPage ) {
    OLEPageList olePL = new OLEPageList();
    
    for(; iStartPage != END_OF_CHAIN; iStartPage = getNextPage( iStartPage ) ) {
      if( iStartPage == SPECIAL_BLOCK ) {
        System.err.println( "unexpected special block" );
        break;
      }
      olePL.addPageIndex( iStartPage );
    }
    
    return olePL;
  }
}
