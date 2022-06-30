/*
 * Copyright (c) 2016 by European Commission
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/european-union-public-licence-eupl-v.1.1
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 * 
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 * 
 */

package eu.eidas.node.auth.util.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.junit.Assert;


public class FileUtils {
    public static void copyFile(File fSource, File fDest) {
        try {
            if (fSource.isDirectory()) {
                String[] fList = fSource.list();
                for (int i = 0; i < fList.length; i++) {
                    File dest = new File(fDest, fList[i]);
                    File source = new File(fSource, fList[i]);
                    copyFile(source, dest);
                }
            }
            else {
                copyOneFile(fSource, fDest);
            }
        }
        catch (Exception ex) {
            Assert.fail("Error initializing the test environment");
        }
    }
    private static void copyOneFile(File fSource, File fDest) throws IOException {
        FileInputStream fis = new FileInputStream(fSource);
        FileOutputStream fos = new FileOutputStream(fDest);
        byte[] buffer = new byte[4*1024];
        int iBytesReads;
        while ((iBytesReads = fis.read(buffer)) >= 0) {
            fos.write(buffer, 0, iBytesReads);
        }
        if (fis != null) {
            fis.close();
        }
        if (fos != null) {
            fos.close();
        }
    }

}
