/*
 * JUnitTest
 *
 * This file is part of the project "iKu Java-Library"
 * (c) 2003 iKu Systemhaus AG
 * Am R&ouml;merkastell 4
 * 66121 Saarbr&uuml;cken
 * GERMANY
 */

package test;

import junit.framework.*;
import junit.textui.*;

/**
 * JUnit-Tests ohne viel Schnick-Schnack starten
 *
 * Design-Pattern-Role: 
 * @version $Id: JUnitTest.java,v 1.1 2004/05/25 06:47:34 kurti Exp $
 * @author Kurt Huwig
 *
 */

public class JUnitTest extends TestCase {
    protected void start() {
        try {
            // sollte die Anwendung sich mit System.exit(0) beenden, müssen wir
            // gegensteuern!
            final Thread successfulExitParachute = new Thread(new Runnable() {
                public void run() {
                    // oh ooh, nix wie raus hier!
                    Runtime.getRuntime().halt(1);
                }
            });
            
            Runtime.getRuntime().addShutdownHook(successfulExitParachute);
            final TestResult testResult =
                    new TestRunner().doRun(new TestSuite(getClass()));
            
            // Anwendung hat sich nicht selbst beendet, also alles klar
            Runtime.getRuntime().removeShutdownHook(successfulExitParachute);
            
            if (!testResult.wasSuccessful()) {
                System.exit(TestRunner.FAILURE_EXIT);
            }
            System.exit(TestRunner.SUCCESS_EXIT);

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(TestRunner.EXCEPTION_EXIT);
        }
    }
}
