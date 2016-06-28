package com.harasoft.relaunch;


import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * http://stackoverflow.com/questions/7485114/how-to-zip-and-unzip-the-files
 * http://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
 */
public class ZipUtil {
/*
    public static void zip(String[] files, String zipFile) throws IOException {
        BufferedInputStream origin = null;
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
        try {
            int BUFFER_SIZE = 8192;
            byte data[] = new byte[BUFFER_SIZE];

            for (int i = 0; i < files.length; i++) {
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);
                try {
                    ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                finally {
                    origin.close();
                }
            }
        }
        finally {
            out.close();
        }
    }*/

    public boolean unzip(String zipFile) {
        try {
            File f = new File(zipFile);
            String location = f.getParent();
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            try {
                ZipEntry ze;
                int count;
                byte[] buffer = new byte[8192];
                while ((ze = zis.getNextEntry()) != null) {
                    File file = new File(location, ze.getName());
                    File dir = ze.isDirectory() ? file : file.getParentFile();
                    if (!dir.isDirectory() && !dir.mkdirs()) {
                        return false;
                    }
                    if (ze.isDirectory()) {
                        continue;
                    }
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        while ((count = zis.read(buffer)) != -1){
                            fout.write(buffer, 0, count);
                        }
                        zis.closeEntry();
                    }finally {
                        fout.close();
                    }
                }
            }finally {
                zis.close();
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

}
