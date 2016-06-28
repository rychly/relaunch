package com.harasoft.relaunch;

/*
 * Copyright (C) 2014 Simple Explorer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

 // edit haron for ReLaunch

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RootCommands {

    private static final String UNIX_ESCAPE_EXPRESSION = "(\\(|\\)|\\[|\\]|\\s|\'|\"|`|\\{|\\}|&|\\\\|\\?)";

    private static String getCommandLineString(String input) {
        return input.replaceAll(UNIX_ESCAPE_EXPRESSION, "\\\\$1");
    }
// переписа под релаунч
    public static ArrayList<String[]> listFiles(String path, boolean showhidden) {
        ArrayList<String[]> mDirContent = new ArrayList<String[]>();
        BufferedReader in;

        try {
            // получили поток с выводом
            in = execute("ls -a -l " + getCommandLineString(path));

            String line;
            if (in == null){
                return null;
            }
            // начинаем разбираться с каждой строкой вывода
            while ((line = in.readLine()) != null) {
                if (!showhidden) {
                    if (line.charAt(0) != '.') {
                        mDirContent.add(getAttrs(line));
                    }
                } else {
                    mDirContent.add(getAttrs(line));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mDirContent;
    }
    // переписана
    private static String[] getAttrs(String string) {
        if (string.length() < 44) {
            throw new IllegalArgumentException("Bad ls -l output: " + string);
        }
        final char[] chars = string.toCharArray();

        final String[] results = new String[11];
        int ind = 0;
        final StringBuilder current = new StringBuilder();

        Loop:
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case ' ':
                case '\t':

                    if (current.length() != 0) {
                        results[ind] = current.toString();
                        ind++;
                        current.setLength(0);
                        if (ind == 10) {
                            results[ind] = string.substring(i).trim();
                            ind++;
                            break Loop;
                        }
                    }
                    break;

                default:
                    current.append(chars[i]);
                    break;
            }
        }
        if (current.length() != 0) {
            results[ind] = current.toString();
        }
        return results;
    }
    // используется как есть
    private static boolean containsIllegals(String toExamine) {
        // checks for "+" sign so the program doesn't throw an error when its
        // not erroring.
        Pattern pattern = Pattern.compile("[+]");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }
    // используется как есть
    private static BufferedReader execute(String cmd) {
        BufferedReader reader;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String err = (new BufferedReader(new InputStreamReader(process.getErrorStream()))).readLine();
            os.flush();
            os.close();
            if (process.waitFor() != 0 || (!"".equals(err) && null != err)&& !containsIllegals(err)) {
               //Log.e("Root Error, cmd: " + cmd, err);
                return null;
            }
            return reader;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }
    // Delete file with root
    public static boolean DeleteFileRoot(String path) {
        boolean result = false;
        try {
            /*if (!readReadWriteFile()) {
                Log.i("===========", "-------- DeleteFileRoot --------");
                return false;
            }*/
                //RootTools.remount(path, "rw");

            if (new File(path).isDirectory()) {
                execute("busybox rm -f -r " + getCommandLineString(path));
            } else {
                execute("busybox rm -r " + getCommandLineString(path));
            }
            result = true;
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return result;
    }
    // Copy with Root Access using RootTools library
    public static boolean CopyFileRoot(String old, String newDir) {
        try {
            /*if (!readReadWriteFile()) {
                Log.i("===========", "-------- CopyFileRoot --------");
                return false;
            }*/
            execute("busybox cp -f -r " + getCommandLineString(old) + " " + getCommandLineString(newDir));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    // Move or Copy with Root Access using RootTools library
    public static boolean MoveFileRoot(String old, String to) {
        try {
            /*if (!readReadWriteFile()) {
                Log.i("===========", "-------- MoveFileRoot --------");
                return false;
            }*/

            execute("busybox mv -f " + getCommandLineString(old) + " " + getCommandLineString(to));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
    // Create Directory with root
    public static boolean CreateDirRoot(String path) {
        try {
            // проверяем в каком режиме подключена файловая система
            /*if (!readReadWriteFile()) {
                Log.i("===========", "-------- CreateDirRoot --------");
                return false;
            }*/
            // выполняем команду создания папки
            execute("busybox mkdir " + getCommandLineString(path));
        } catch (Exception e) {
            return false;
        }
        return true;

    }
    public static boolean applyPermissions(File file, Permissions permissions) {
        try {
            /*if (!readReadWriteFile())
                RootTools.remount(file.getAbsolutePath(), "rw");*/
            execute("busybox chmod " + Permissions.toOctalPermission(permissions) + " "
                    + getCommandLineString(file.getAbsolutePath()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
    /*
    // Check if system is mounted
    private static boolean readReadWriteFile() {
        File mountFile = new File("/proc/mounts");
        StringBuilder procData = new StringBuilder();
        if (mountFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(mountFile.toString());
                DataInputStream dis = new DataInputStream(fis);
                BufferedReader br = new BufferedReader(new InputStreamReader(dis));
                String data;
                while ((data = br.readLine()) != null) {
                    procData.append(data).append("\n");
                }

                br.close();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            String[] tmp = procData.toString().split("\n");
            for (String aTmp : tmp) {
                Log.i("================ ","--------- aTmp= " + aTmp);
                // Kept simple here on purpose different devices have
                // different blocks
                if (aTmp.contains("/dev/block") && aTmp.contains("/system")) {
                    Log.i("================ ","--------- aTmp= " + aTmp);
                    if (aTmp.contains("rw")) {
                        // system is rw
                        return true;
                    } else if (aTmp.contains("ro")) {
                        // system is ro
                        return false;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }
*/

/*
        public static ArrayList<String> findFiles(String path, String query) {
            ArrayList<String> mDirContent = new ArrayList<String>();
            BufferedReader in;

            try {
                in = execute("find " + getCommandLineString(path) + " -type f -iname " + '*' + getCommandLineString(query) + '*' + " -exec ls -a {} \\;");

                String line;
                while ((line = in.readLine()) != null) {
                    mDirContent.add(line);
                }
            } catch (IOException e) {
                e.printStackTre();
            }

            return mDirContent;
        }

    public static String[] getFileProperties(File file) {
        BufferedReader in;
        String[] info = null;
        String line;


        try {
            in = execute("ls -l " + getCommandLineString(file.getAbsolutePath()));

            while ((line = in.readLine()) != null) {
                info = getAttrs(line);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return info;
    }
    */
}

