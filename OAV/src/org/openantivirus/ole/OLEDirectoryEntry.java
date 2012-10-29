/*
 * OLEPropertyStorage.java
 *
 * Created on 13. September 2001, 22:18
 */

package org.openantivirus.ole;

/**
 *
 * @author  Kurt Huwig
 * @version 
 */
public class OLEDirectoryEntry {
  public final static int SIZE = 0x80;
  
  public final static int
    TYPE_STORAGE = 1,
    TYPE_STREAM  = 2,
    TYPE_ROOT    = 5;
  
  private String sName;
  
  /** Holds value of property type. */
  private int type;
  
  /** Holds value of property previous. */
  private int leftChild;
  
  /** Holds value of property next. */
  private int rightChild;
  
  /** Holds value of property directory. */
  private int directory;
  
  /** Holds value of property start. */
  private int start;
  
  /** Holds value of property size. */
  private int size;
  
  /** Creates new OLEPropertyStorage */
  public OLEDirectoryEntry(OLEPage olePage, int iStart) {
    int iNameLength = olePage.getShort( iStart + 0x40 ) - 2;
    
    StringBuffer sb = new StringBuffer();
    for( int i = 0; i < iNameLength; i += 2 ) {
      sb.append( (char) olePage.getByte( iStart + i ) );
    }
    sName = sb.toString();
    
    type       = olePage.getByte( iStart + 0x42 );
    leftChild  = olePage.getInt(  iStart + 0x44 );
    rightChild = olePage.getInt(  iStart + 0x48 );
    directory  = olePage.getInt(  iStart + 0x4c );
    start      = olePage.getInt(  iStart + 0x74 );
    size       = olePage.getInt(  iStart + 0x78 );
  }
  
  /** Getter for property type.
   * @return Value of property type.
   */
  public int getType() {
    return type;
  }
  
  /** Getter for property previous.
   * @return Value of property previous.
   */
  public int getLeftChild() {
    return leftChild;
  }
  
  /** Getter for property next.
   * @return Value of property next.
   */
  public int getRightChild() {
    return rightChild;
  }
  
  /** Getter for property directory.
   * @return Value of property directory.
   */
  public int getDirectory() {
    return directory;
  }
  
  /** Getter for property start.
   * @return Value of property start.
   */
  public int getStart() {
    return start;
  }
  
  /** Getter for property size.
   * @return Value of property size.
   */
  public int getSize() {
    return size;
  }
  
  public String getName() {
    return sName;
  }
}
