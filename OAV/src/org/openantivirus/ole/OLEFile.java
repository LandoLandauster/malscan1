package org.openantivirus.ole;

import java.io.*;
import java.util.*;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEFile.java,v 1.2 2005/09/03 16:16:40 kurti Exp $
 */
public class OLEFile {
  private final File file;
  private long maxPage;
  private OLEFileInformationBlock oleFIB;
  private RandomAccessFile raf;
  private OLEPageMap oleBBPM = new OLEPageMap();
  private OLEPageMap oleDPM = new OLEPageMap();
  private OLEDirectory oleDir = new OLEDirectory();
  
  /** Creates new OLEFile */
  public OLEFile( File file ) throws IOException {
    this.file = file;
    maxPage = file.length() / OLEPage.SIZE - 2;
    
    raf = new RandomAccessFile( file, "r" );
    
    oleFIB = new OLEFileInformationBlock( getPage( -1 ) );
    for( int i = 0; i < oleFIB.getNumBigBlocks(); i++ ) {
      oleBBPM.addBlockPage(
        getOLEPage( oleFIB.getBigBlockPageIndex( i ) )
      );
    }
    
    OLEPageList olePL = oleBBPM.getPageList( oleFIB.getRootChainStart() );
    for( Iterator i = olePL.iterator(); i.hasNext(); ) {
      oleDir.addDirectoryPage(
        getOLEPage( ( (Integer) i.next() ).intValue() )
      );
    }
    
    olePL = oleBBPM.getPageList( oleFIB.getSmallBlockStart() );
    for( Iterator i = olePL.iterator(); i.hasNext(); ) {
      oleDPM.addBlockPage(
        getOLEPage( ( (Integer) i.next() ).intValue() )
      );
    }
    
  }
  
  public void close() throws IOException {
    raf.close();
  }

  public OLEFileInformationBlock getFileInformationBlock() {
    return oleFIB;
  }
    
  public byte[] getPage( int iIndex ) throws IOException {
    byte[] abPage = new byte[ OLEPage.SIZE ];
    
    raf.seek( OLEPage.SIZE * ( iIndex + 1 ) );
    raf.readFully( abPage );
    
    return abPage;
  }
  
  public OLEPage getOLEPage( int iIndex ) throws IOException {
    return new OLEPage( getPage( iIndex ) );
  }
  
  public OLEPageMap getBigBlockPageMap() {
    return oleBBPM;
  }
  
  public OLEDirectory getDirectory() {
    return oleDir;
  }
  
  public OLEPageMap getSmallBlockPageMap() {
    return oleDPM;
  }
  
  public List getDirectoryEntryList() {
    return oleDir.getDirectoryEntryList();
  }

public File getFile() {
    return file;
}

public long getMaxPage() {
    return maxPage;
}
}
