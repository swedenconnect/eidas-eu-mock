/*
 * Copyright 2013 Swedish E-identification Board (E-legitimationsnämnden)
 *  		 
 *   Licensed under the EUPL, Version 1.1 or ñ as soon they will be approved by the 
 *   European Commission - subsequent versions of the EUPL (the "Licence");
 *   You may not use this work except in compliance with the Licence. 
 *   You may obtain a copy of the Licence at:
 * 
 *   http://joinup.ec.europa.eu/software/page/eupl 
 * 
 *   Unless required by applicable law or agreed to in writing, software distributed 
 *   under the Licence is distributed on an "AS IS" basis,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 *   implied.
 *   See the Licence for the specific language governing permissions and limitations 
 *   under the Licence.
 */
package se.swedenconnect.eidas.test.cef20demohub.utils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File operations
 */
public class FileOps {

    private static final Logger LOG = Logger.getLogger(FileOps.class.getName());


    public static byte[] readBinaryFile(File file) {
        List inp = new LinkedList<Byte>();
        try {
            FileInputStream fi = new FileInputStream(file);
            try {
                while (fi.available() > 0) {
                    inp.add(fi.read());
                }
            } finally {
                fi.close();
            }
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
            return new byte[0];
        }
        byte[] b = new byte[inp.size()];
        int i = 0;
        for (Object o : inp) {
            int val = (Integer) o;
            b[i++] = (byte) val;
        }
        return b;
    }


    public static byte[] openAndReadBinaryFile(File fileDir) {
        JFileChooser fc = new JFileChooser(fileDir);
        int returnVal = fc.showOpenDialog(fc);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.canRead()) {
                byte[] result = readBinaryFile(file);
                return result;
            }
        }
        return null;
    }

    static public void saveByteFile(byte[] data, File file) {

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
        } catch (IOException ex) {
            LOG.warning(ex.getMessage());
        }
    }
}
