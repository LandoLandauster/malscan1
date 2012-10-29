/*
 * $Id: SignTool.java,v 1.3 2002/04/10 16:29:27 kurti Exp $
 *
 * This file is part of the OpenAntiVirus-Project,
 * see http://www.openantivirus.org/
 * (c) 2001 iKu Netzwerkl&ouml;sungen
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.openantivirus.util;

import java.io.*;
import java.security.*;
import java.security.spec.*;

/**
 * SignTool
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig
 * @version $Revision: 1.3 $
 */
public class SignTool {
    public static final String VERSION =
        "$Id: SignTool.java,v 1.3 2002/04/10 16:29:27 kurti Exp $";
    
    public static void main(String[] args) {
        try {
            if (args.length != 2) {
                System.err.println("Usage: " + SignTool.class.getName()
                        + " <privatekey.pkcs8> <virussignatures>");
                System.exit(1);
            }

            // Read private key
            File fKey = new File(args[0]);
            int iLength = (int) fKey.length();
            byte[] abKey = new byte[iLength];
            InputStream is = new FileInputStream(fKey);
            for (int iRead = 0; iRead < iLength; ) {
                int i = is.read(abKey, iRead, iLength - iRead);
                if (i == -1) {
                    System.err.println("Error while reading key");
                    System.exit(1);
                }
                iRead += i;
            }
            is.close();

            // init signature
            PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(abKey);
            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);
            Signature signature = Signature.getInstance("DSA");
            signature.initSign(priKey);
            final MessageDigest md = MessageDigest.getInstance("SHA1");
            final DigestInputStream dis = new DigestInputStream(
                    new FileInputStream(args[1]), md);
            
            // read file to be signed
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String sLine;
            while ((sLine = br.readLine()) != null) {
                signature.update(sLine.getBytes());
                System.out.println(sLine);
            }
            br.close();
            
            signature.update(md.digest());
            byte[] abSignature = signature.sign();
            
            // print signature
            StringBuffer sbSignature = new StringBuffer();
            for (int i = 0; i < abSignature.length; i++) {
                String sHex = Integer.toHexString(abSignature[i]);
                sbSignature.append(("0" + sHex).substring(sHex.length() - 1));
            }
            
            System.out.println("==" + sbSignature.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
