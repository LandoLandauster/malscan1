package org.openantivirus.ole;

import java.io.*;
import java.util.*;

/**
 *
 * @author  Kurt Huwig
 * @version $Id: OLEReader.java,v 1.4 2005/09/03 16:16:40 kurti Exp $
 */
public class OLEReader {
  private ArrayList listMacroPages = new ArrayList();
  private int iMacroSize;
  
  /** Creates new OLEReader */
  public OLEReader( String sFileName ) throws IOException {
    OLEFile oleFile = new OLEFile( new File( sFileName ) );
    
    oleFile.getFileInformationBlock();
    oleFile.close();
    
    OLEPageMap oleBBPM = oleFile.getBigBlockPageMap();
    OLEPageMap oleSBPM = oleFile.getSmallBlockPageMap();
    
    OLEDirectory oleDir = oleFile.getDirectory();
    OLEPageList olePLRoot = null;
    
    for( int i = 0; i < oleDir.size(); i++ ) {
      OLEDirectoryEntry oleDE = oleDir.getDirectoryEntry( i );
        /*
        System.out.println( i + ": " + oleDE.getName() );
        System.out.println( "  Size:  " + oleDE.getSize() );
        System.out.println( "  Start: " + oleDE.getStart() );
        System.out.println( "  Left:  " + oleDE.getLeftChild() );
        System.out.println( "  Right: " + oleDE.getRightChild() );
        System.out.println( "  Dir:   " + oleDE.getDirectory() );
         */
      if( oleDE.getName().equals( "Root Entry" ) ) {
        olePLRoot = oleBBPM.getPageList( oleDE.getStart() );
      } else if( oleDE.getName().equals( "ThisDocument" ) ) {
        iMacroSize = oleDE.getSize();
        OLEPageList olePL =
        oleDE.getSize() >= 0x1000 ?
        oleBBPM.getPageList( oleDE.getStart() ) :
          oleSBPM.getPageList( oleDE.getStart() );
          
          //          FileOutputStream fos = new FileOutputStream( "macro.bin" );
          int iRemaining = oleDE.getSize();
          for(
          Iterator it = olePL.iterator(); iRemaining > 0 && it.hasNext();
          ) {
            int iSmallBlockOffset = ( (Integer)it.next() ).intValue();
            if( oleDE.getSize() < 0x1000 ) {
              iSmallBlockOffset = olePLRoot.getPageIndex( iSmallBlockOffset );
            }
            listMacroPages.add( new Integer( iSmallBlockOffset ) );
            //            byte[] ab = oleFile.getPage( iSmallBlockOffset );
            //            fos.write( ab, 0, Math.min( OLEPage.SIZE, iRemaining ) );
            iRemaining -= OLEPage.SIZE;
          }
          //          fos.close();
      }
    }
    
    oleFile.close();
  }
  
  public int getMacroSize() {
    return iMacroSize;
  }
  
  /**
   * return absolute PageIndices in the file, i.e. -1 == first block -> 0
   */
  public int[] getPageIndices() {
    int[] ai = new int[ listMacroPages.size() ];
    for( int i = 0; i < ai.length; i++ ) {
      ai[ i ] = ( (Integer)listMacroPages.get( i ) ).intValue() + 1;
    }
    return ai;
  }
  
  /**
   * @param args the command line arguments
   */
  /*
  public static void main(String args[]) {
    try {
      new OLEReader( "/home/kurt/This is a test document with a macro in it.doc" );
//      new OLEReader( "/home/kurt/Werkzeug.doc" );
    } catch( IOException ioe ) {
      ioe.printStackTrace();
    }
  } 
   */ 
}
