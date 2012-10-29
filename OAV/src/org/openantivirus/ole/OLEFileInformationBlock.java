package org.openantivirus.ole;

import java.io.*;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEFileInformationBlock.java,v 1.1 2001/09/15 06:44:34 kurti Exp $
 */
public class OLEFileInformationBlock extends OLEPage {
  public final static byte[] MAGIC = {
    (byte)0xd0, (byte)0xcf, (byte)0x11, (byte)0xe0,
    (byte)0xa1, (byte)0xb1, (byte)0x1a, (byte)0xe1
  };
  
  /** Holds value of property rootChainStart. */
  private int rootChainStart;
  
  /** Holds value of property numBigBlocks. */
  private int numBigBlocks;
  
  /** Holds value of property smallBlockStart. */
  private int smallBlockStart;
  
  /** Creates new OLEFileInformationBlock */
  public OLEFileInformationBlock( byte[] abPage ) throws IOException {
    super( abPage );
    
    for( int i = 0; i < MAGIC.length; i++ ) {
      if( abPage[ i ] != MAGIC[ i ] ) {
        throw new IOException( "Not an OLE file" );
      }
    }
    
    numBigBlocks    = getInt( 0x2c );
    rootChainStart  = getInt( 0x30 );
    smallBlockStart = getInt( 0x3c );
  }  
  
  /** Getter for property rootChainStart.
   * @return Value of property rootChainStart.
   */
  public int getRootChainStart() {
    return rootChainStart;
  }
  
  /** Getter for property numBigBlocks.
   * @return Value of property numBigBlocks.
   */
  public int getNumBigBlocks() {
    return numBigBlocks;
  }
  
  /** Getter for property smallBlockStart.
   * @return Value of property smallBlockStart.
   */
  public int getSmallBlockStart() {
    return smallBlockStart;
  }
  
  /** Indexed getter for property bigBlockPageIndex.
   * @param iIndex Index of the property.
   * @return Value of the property at <CODE>index</CODE>.
   */
  public int getBigBlockPageIndex( int iIndex ) {
    return getInt( 0x4c + iIndex * 4 );
  }
  
}
