package com.harasoft.relaunch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import it.sauronsoftware.ftp4j.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class FTPConnector {

    FTPClient client;
    String addressServer;
    int portConnectServer;
    String loginServer;
    String passwordServer;
    String rootPath;

    public FTPConnector(int id_ftp, Context context) {
        MyDBHelper dbHelper = new MyDBHelper(context, "FTP");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        if (db == null){
            return;
        }
        Cursor c = db.query("FTP", null, "ID = ?", new String[] { String.valueOf(id_ftp) }, null, null, null);
        if (c == null || !(c.getCount() > 0)){
            return;
        }
        c.moveToFirst();
        for (String cn : c.getColumnNames()) {
            if (cn.equals("SERVER")){
                addressServer = c.getString(c.getColumnIndex(cn));
            }
            if (cn.equals("PATH")){
                rootPath = c.getString(c.getColumnIndex(cn));
            }
            if (cn.equals("LOGIN")){
                loginServer = c.getString(c.getColumnIndex(cn));
            }
            if (cn.equals("PASSWORD")){
                passwordServer = c.getString(c.getColumnIndex(cn));
            }
            if (cn.equals("PORT")){
                portConnectServer = c.getInt(c.getColumnIndex(cn));
            }
        }
        c.close();
        db.close();
        dbHelper.close();
    }
    //======================== открытие и закрытие соединения ======
    private boolean connect(String address, int port, String login, String password){
        client = new FTPClient();
        boolean connRez;
        if (port == -1){
            port = 21;
        }
        if (address == null){
            return false;
        }
        if(address.startsWith("FTP| ")) {
            address = address.substring("FTP| ".length());
        }
        try {
            client.connect(address, port);
            if (login != null && login.length() > 0){
                client.login(login, password);
            }else{
                client.login("anonymous", "anonymous");
            }
            connRez = true;
        } catch (IOException e) {
            connRez = false;
        } catch (FTPIllegalReplyException e) {
            connRez = false;
        } catch (FTPException e) {
            connRez = false;
        }
        return connRez;
    }
    private boolean disconnected(){
        boolean connRez;
        try {
            client.disconnect(true);
            connRez = true;
        } catch (IOException e) {
            connRez = false;
        } catch (FTPIllegalReplyException e) {
            connRez = false;
        } catch (FTPException e) {
            connRez = false;
        }
        return connRez;
    }
    // ================================================================
    public FTPFile[] ftpFilesList(String Path){
        FTPFile[] fileFTP = null;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            try {
                client.changeDirectory(Path);
                fileFTP = client.list();
            } catch (IOException e) {
                fileFTP = null;
            } catch (FTPIllegalReplyException e) {
                fileFTP = null;
            } catch (FTPException e) {
                fileFTP = null;
            } catch (FTPDataTransferException e) {
                fileFTP = null;
            } catch (FTPAbortedException e) {
                fileFTP = null;
            } catch (FTPListParseException e) {
                fileFTP = null;
            }
            disconnected();
        }
        return fileFTP;
    }
    public boolean delete(String delPath, boolean dirDel){
        boolean delRez = false;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(delPath.startsWith("FTP| ")) {
                delPath = delPath.substring("FTP| ".length());
            }
            try {
                if (dirDel){
                    client.deleteDirectory(delPath);
                }else{
                    client.deleteFile(delPath);
                }
                delRez = true;
            } catch (IOException e) {
                delRez = false;
            } catch (FTPIllegalReplyException e) {
                delRez = false;
            } catch (FTPException e) {
                delRez = false;
            }
        }
        disconnected();
        return delRez;
    }
    public boolean createDir(String newDir){
        boolean createRez = false;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(newDir.startsWith("FTP| ")) {
                newDir = newDir.substring("FTP| ".length());
            }
            try {
                client.createDirectory(newDir);
                createRez = true;
            } catch (IOException e) {
                createRez = false;
            } catch (FTPIllegalReplyException e) {
                createRez = false;
            } catch (FTPException e) {
                createRez = false;
            }
        }
        disconnected();
        return createRez;
    }
    public boolean rename(String oldName, String newName){
        boolean renRez = false;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(oldName.startsWith("FTP| ")) {
                oldName = oldName.substring("FTP| ".length());
            }
            if(newName.startsWith("FTP| ")) {
                newName = newName.substring("FTP| ".length());
            }
            try {
                client.rename(oldName, newName);
                renRez = true;
            } catch (IOException e) {
                renRez = false;
            } catch (FTPIllegalReplyException e) {
                renRez = false;
            } catch (FTPException e) {
                renRez = false;
            }
        }
        disconnected();
        return renRez;
    }
    public boolean download(String sourceDir, String sourceFile, String destDir){
        // получаем список элементов родительской
        // определяем папка или файл
        //
        boolean renRez = false;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            disconnected();
            // проверяем наличие приемной папки. создаем если надо
            File checkDir = new File(destDir);
            if(!checkDir.exists()){
                if(!checkDir.mkdirs()){
                    renRez = false;
                }
            }
            // избавляемся от приставки FTP
            if(sourceDir.startsWith("FTP| ")) {
                sourceDir = sourceDir.substring("FTP| ".length());
            }
            // определяем папка или файл
            FTPFile[] FTPlist = ftpFilesList(sourceDir);
            int typeSource = -1;
            for (FTPFile aFTPlist : FTPlist) {
                if (sourceFile.equals(aFTPlist.getName())) {
                    typeSource = aFTPlist.getType();
                }
            }
            // если не определили тип выходим с ошибкой
            if (typeSource == -1){
                return false;
            }
            // начинаем копирование
            if (typeSource == FTPFile.TYPE_DIRECTORY) { // если копируем папку
                // создаем подпапку
                checkDir = new File(destDir + File.separator + sourceFile);
                if (!checkDir.exists()) {
                    if (!checkDir.mkdirs()) {
                        renRez = false;
                    }
                }
                // получаем содержимое папки
                FTPlist = ftpFilesList(sourceDir);
                // перебираем элементы
                for (FTPFile aFTPlist : FTPlist) {
                    renRez = download(sourceDir + File.separator + sourceFile, aFTPlist.getName(), destDir + File.separator + sourceFile);
                }
            }else{
                renRez = downloadFile(sourceDir + File.separator + sourceFile, destDir);
            }
        }
        return renRez;
    }
    public boolean downloadFile(String source, String dest){
        boolean renRez = false;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(source.startsWith("FTP| ")) {
                source = source.substring("FTP| ".length());
            }
            File checkDir = new File(dest);
            if(!checkDir.exists()){
                if(!checkDir.mkdirs()){
                    return false;
                }
            }
            String destFileName = dest + File.separator + source.substring(source.lastIndexOf("/")+1);
            File localFile = new File(destFileName);
            try {
                client.download(source, localFile);
                renRez = true;
            } catch (IOException e) {
                renRez = false;
            } catch (FTPIllegalReplyException e) {
                renRez = false;
            } catch (FTPException e) {
                renRez = false;
            } catch (FTPAbortedException e) {
                renRez = false;
            } catch (FTPDataTransferException e) {
                renRez = false;
            }
        }
        disconnected();
        return renRez;
    }
    public boolean upload(String sourceDir, String sourceFile, String destDir){
        // получаем список элементов родительской
        // определяем папка или файл
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            boolean ff;
            // проверяем наличие приемной папки. создаем если надо
            if(destDir.startsWith("FTP| ")) {
                destDir = destDir.substring("FTP| ".length());
            }
            try {
                client.changeDirectory(destDir);
                ff = true;
            } catch (IOException e) {
                ff = false;
            } catch (FTPIllegalReplyException e) {
                ff = false;
            } catch (FTPException e) {
                ff = false;
            }
            disconnected();
            if(!ff){
                if(!createDir(destDir)){
                    return false;
                }
            }
            // определяем папка или файл
            String from = sourceDir + File.separator + sourceFile;
            File src = new File(from);
            if (src.isFile()) {
                if (!uploadFile(from, destDir)){
                    return false;
                }
            } else {
                String[] strDirList = src.list();
                String to = destDir + File.separator + sourceFile;
                if(!createDir(to)){
                    return false;
                }
                for (String aStrDirList1 : strDirList) {
                    File f1 = new File(from +"/" + aStrDirList1);
                    if (f1.isFile()) {
                        if (!uploadFile(from + "/" + aStrDirList1, to)){
                            return false;
                        }
                    } else {
                        if (!upload(from, aStrDirList1, destDir + File.separator + sourceFile)){
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
    public boolean uploadFile(String source, String dest){
        boolean renRez = false;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(dest.startsWith("FTP| ")) {
                dest = dest.substring("FTP| ".length());
            }
            try {
                client.changeDirectory(dest);
                client.upload(new File(source));
                renRez = true;
            } catch (IOException e) {
                renRez = false;
            } catch (FTPIllegalReplyException e) {
                renRez = false;
            } catch (FTPException e) {
                renRez = false;
            } catch (FTPAbortedException e) {
                renRez = false;
            } catch (FTPDataTransferException e) {
                renRez = false;
            }
        }
        disconnected();
        return renRez;
    }
    public long getFileSize(String source){
        long renRez = 0;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(source.startsWith("FTP| ")) {
                source = source.substring("FTP| ".length());
            }
            try {
                renRez = client.fileSize(source);
            } catch (IOException e) {
                renRez = 0;
            } catch (FTPIllegalReplyException e) {
                renRez = 0;
            } catch (FTPException e) {
                renRez = 0;
            }
        }
        disconnected();
        return renRez;
    }
    public Date getModifiedDate(String source){
        Date renRez = null;
        if(connect(addressServer, portConnectServer, loginServer, passwordServer)) {
            if(source.startsWith("FTP| ")) {
                source = source.substring("FTP| ".length());
            }
            try {
                renRez = client.modifiedDate(source);
            } catch (IOException e) {
                renRez = null;
            } catch (FTPIllegalReplyException e) {
                renRez = null;
            } catch (FTPException e) {
                renRez = null;
            }
        }
        disconnected();
        return renRez;
    }

}
