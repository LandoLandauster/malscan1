/*
 * $Id: SignatureKeyGenerator.java,v 1.3 2004/05/01 14:36:11 kurti Exp $
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
import java.security.interfaces.*;
import java.security.spec.*;

/**
 * Generates a RSA signature
 *
 * Pattern-Roles: 
 * @author  Kurt Huwig
 * @version $Revision: 1.3 $
 */
public class SignatureKeyGenerator {
    public static final String VERSION =
        "$Id: SignatureKeyGenerator.java,v 1.3 2004/05/01 14:36:11 kurti Exp $";
    
    public static void main(String[] args) {
        try {
            System.out.print("Please give a seed (characters) >");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            String seed = br.readLine();
            
            KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(seed.getBytes());
            generator.initialize(1024, random);

            KeyPair keyPair = generator.genKeyPair();
            DSAPublicKey puk = (DSAPublicKey) keyPair.getPublic();
            DSAParams params = puk.getParams();
            System.out.println("P: " + params.getP());
            System.out.println("Q: " + params.getQ());
            System.out.println("G: " + params.getG());
            System.out.println("Y: " + puk.getY());
            
            DSAPrivateKey prk = (DSAPrivateKey) keyPair.getPrivate();
            params = prk.getParams();
            System.out.println("X: " + prk.getX());
            
            OutputStream os = new FileOutputStream("publickey.x509");
            os.write(puk.getEncoded());
            os.close();
            
            os = new FileOutputStream("privatekey.pkcs8");
            os.write(prk.getEncoded());
            os.close();
            
            PKCS8EncodedKeySpec priKeySpec = new PKCS8EncodedKeySpec(prk.getEncoded());

            KeyFactory keyFactory = KeyFactory.getInstance("DSA");
            PrivateKey priKey = keyFactory.generatePrivate(priKeySpec);

            Signature signature = Signature.getInstance("SHA1withDSA");
            signature.initSign(priKey);
            
            br = new BufferedReader(new FileReader("/home/kurt/SourceForge/openantivirus/java/signatures/virussignatures.txt"));
            String sLine;
            while ((sLine = br.readLine()) != null) {
                signature.update(sLine.getBytes());
            }
            br.close();
            
            System.out.println("signing...");
            byte[] dasig = signature.sign();
            System.out.println("signed");
            
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(puk.getEncoded());

            PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

            Signature sig = Signature.getInstance("SHA1withDSA");
            sig.initVerify(pubKey);
            
            br = new BufferedReader(new FileReader("/home/kurt/SourceForge/openantivirus/java/signatures/virussignatures.txt"));
            while ((sLine = br.readLine()) != null) {
                sig.update(sLine.getBytes());
            }
            br.close();
            
            System.out.println("verifying...");
            System.out.println(sig.verify(dasig));
            System.out.println("verified");

            
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
