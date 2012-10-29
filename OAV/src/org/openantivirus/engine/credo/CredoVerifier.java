/*
 * $Id: CredoVerifier.java,v 1.1 2003/12/14 11:08:26 kurti Exp $
 * 
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is OAV.
 *
 * The Initial Developer of the Original Code is Kurt Huwig <kurt@huwig.de>.
 * Portions created by the Initial Developer are Copyright (C) 2001-2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *
 * ***** END LICENSE BLOCK ***** */

package org.openantivirus.engine.credo;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.cert.Certificate;

/**
 * Verifies digital signatures of Credo files
 *
 * Pattern-Roles:
 * @author  Kurt Huwig <kurt@huwig.de>
 * @version $Revision: 1.1 $
 */
public class CredoVerifier {
    public static final String VERSION =
        "$Id: CredoVerifier.java,v 1.1 2003/12/14 11:08:26 kurti Exp $";
    
    /** Number of signature levels */
    public static final int SIGNATURE_LEVELS = 4;
    
    /** Path to the signing certificate */
    private static final String CERTIFICATE_PATH = "/oav.cer";
    
    /** Type of the certificate */
    private static final String CERTIFICATE_TYPE = "X.509";
    
    /**
     * There can be only one! We trust noone besides ourselves :-)
     */
    private static final Certificate[] oavCertificate =
            new Certificate[SIGNATURE_LEVELS];
    
    private static final PublicKey[] oavPublicKey =
            new PublicKey[SIGNATURE_LEVELS];
    
    static {
        for (int level = 0; level < SIGNATURE_LEVELS; level++) {
            try {
                final InputStream is = CredoEntry.class.getResourceAsStream(
                        CERTIFICATE_PATH + ".level" + (level + 1));
                final CertificateFactory cf = CertificateFactory.getInstance(
                        CERTIFICATE_TYPE);
                oavCertificate[level] = cf.generateCertificate(is);
                try {
                    is.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                oavPublicKey[level] = oavCertificate[level].getPublicKey();
            } catch (CertificateException ce) {
                ce.printStackTrace();
            }
        }
    }
    
    /**
     * Verifies the digital signature of this entry; the data of the entry
     * has to be read completely before calling this method.
     *
     * @throws CredoException If the digital signature is invalid
     * @return signature level
     */
    public static int verify(CredoEntry credoEntry) throws CredoException {
        final Certificate[] certificates =
                credoEntry.getJarEntry().getCertificates();
        if (certificates == null) {
            throw new CredoException(
                    "No signature found or entry not fully read");
        }
        int verifiedLevel = -1;
cert:   for (int i = 0; i < certificates.length; i++) {
            for (int level = 0; level < SIGNATURE_LEVELS; level++) {
                if (certificates[i].equals(oavCertificate[level])) {
                    continue cert;
                }
            }
            for (int level = 0; level < SIGNATURE_LEVELS; level++) {
                try {
                    certificates[i].verify(oavPublicKey[level]);
                    System.out.println("  signed by '"
                            + ((X509Certificate)certificates[i])
                              .getSubjectDN() + "'");
                    verifiedLevel = level;
                    //break cert;
                } catch (Exception e) {
                    // we have several certificates; all but one will fail
                }
            }
        }
        if (verifiedLevel == -1) {
            throw new CredoException("No valid signing certificate found");
        }
        return (verifiedLevel + 1);
    }    
}
