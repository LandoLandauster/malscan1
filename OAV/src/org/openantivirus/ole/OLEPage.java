package org.openantivirus.ole;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEPage.java,v 1.1 2001/09/15 06:44:34 kurti Exp $
 */
public class OLEPage {
  public final static int SIZE = 0x200;
  
  private byte[] abPage;
  
  /** Creates new OLEPage */
  public OLEPage( byte[] abPage ) {
    this.abPage = abPage;
  }
  
  public int getInt( int iOffset ) {
    return ( abPage[ iOffset ]     <<  0 ) |
           ( abPage[ iOffset + 1 ] <<  8 ) |
           ( abPage[ iOffset + 2 ] << 16 ) |
           ( abPage[ iOffset + 3 ] << 24 );
  }
  
  public int getShort( int iOffset ) {
    return ( abPage[ iOffset ]     <<  0 ) |
           ( abPage[ iOffset + 1 ] <<  8 );
  }
  
  public int getByte( int iOffset ) {
    return abPage[ iOffset ];
  }
}
