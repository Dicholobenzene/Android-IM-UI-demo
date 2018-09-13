package aria.myapp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.http.util.EncodingUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Created by aria on 2017/5/26.
 */

public class ftpMethods extends Thread {
    public static class user implements Serializable {
        private String id, job, hobby, latitude, longitude;
        private int room;

        public user(String id, String job, String hobby, String latitude, String longitude) {
            this.id = id;
            this.job = job;
            this.hobby = hobby;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public void setRoom(int room) {
            this.room = room;
        }

        public int getRoom() {
            return room;
        }

        public String getID() {
            return id;
        }

        public String getJob() {
            return job;
        }

        public String getHobby() {
            return hobby;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }
    }


    public static String ftpUpload(String url, String port, String username, String password, String remotePath, String filePath, String fileName) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        String returnMessage = "0";
        try {
            ftpClient.connect(url, Integer.parseInt(port));
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(remotePath);
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(10240);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                fis = new FileInputStream(filePath + fileName);
                ftpClient.storeFile(fileName, fis);
                returnMessage = "1";   //上传成功
            } else {// 如果登录失败
                returnMessage = "0";
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            //IOUtils.closeQuietly(fis);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        return returnMessage;
    }

    public void ftpStreamUpload(String url,String port,String username,String password,String remotePath,String fileName,FileInputStream stream){
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        String returnMessage = "0";
        try {
            ftpClient.connect(url, Integer.parseInt(port));
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(remotePath);
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(10240);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                ftpClient.storeFile(fileName,stream);
                returnMessage = "1";   //上传成功
            } else {// 如果登录失败
                returnMessage = "0";
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            //IOUtils.closeQuietly(fis);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        return ;
    }



    public static String ftpDelete(String url, String port, String username, String password, String remotePath, String fileName) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        String returnMessage = "0";
        try {
            ftpClient.connect(url, Integer.parseInt(port));
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(remotePath);
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
//                ftpClient.setBufferSize(1024);
//                ftpClient.setControlEncoding("UTF-8");
//                ftpClient.enterLocalPassiveMode();
                ftpClient.deleteFile(fileName);
                returnMessage = "1";   //上传成功
            } else {// 如果登录失败
                returnMessage = "0";
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            //IOUtils.closeQuietly(fis);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        return returnMessage;
    }

    public static String ftpDownload(String url, String port, String username, String password, String remotePath, String filePath,String fileName) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis = null;
        String returnMessage = "0";
        try {
            ftpClient.connect(url, Integer.parseInt(port));
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(remotePath);
                // 设置下载目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(10240);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                FTPFile[] files = ftpClient.listFiles(fileName);
                if(files.length<=15)return "0";
                File localPath = new File(filePath+fileName);
                localPath.delete();
                localPath = new File(filePath+fileName);
                OutputStream output = new FileOutputStream(localPath,true);
                InputStream input = ftpClient.retrieveFileStream(fileName);
                byte[] b = new byte[10240];
                input.read(b);
                returnMessage = EncodingUtils.getString(b, "UTF-8");
                output.write(b);
                output.flush();
                output.close();
                input.close();
            }


        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("FTP客户端出错！", e);
        } finally {
            //IOUtils.closeQuietly(fis);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("关闭FTP连接发生异常！", e);
            }
        }
        return returnMessage;
    }

}
