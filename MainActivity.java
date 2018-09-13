package aria.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.http.util.EncodingUtils;

public class MainActivity extends AppCompatActivity {
    String myID ;
    String roomID;
    ftpMethods.user myinfo;
    int FLAG=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = this.getIntent();
        myinfo = (ftpMethods.user) intent.getSerializableExtra("user");
        myID = myinfo.getID();
        roomID= String.valueOf(myinfo.getRoom());
    }
    public int writeFileData(String fileName, String message) {
        try {
            byte[] c=new byte[2];
            c[0]=0x0d;
            c[1]=0x0a;//用于输入换行符的字节码
            String old = readFileData(fileName);
            FileOutputStream fout =openFileOutput(fileName,MODE_PRIVATE);
            byte[] bytes = message.getBytes();
            byte[] olds = old.getBytes();
            fout.write(olds);
            fout.write(c);
            fout.write(bytes);
            fout.close();
            return 0;
        } catch (Exception e) {
            System.out.print("1");
            return 1;
        }
    }
    public int replaceFileData(String fileName, String message) {
        try {
            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
            byte[] bytes = message.getBytes();
            fout.write(bytes);
            fout.close();
            return 0;
        } catch (Exception e) {
            System.out.print("1");
            return 1;
        }
    }
    public String readFileData(String fileName) {
        String res = "";
        try {
            FileInputStream fin = openFileInput(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = EncodingUtils.getString(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    String ftpURL = "192.168.3.29";
    String ftpPort ="81";

    Runnable ftpDown = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            String b =a.ftpDownload(ftpURL,"81","root","","/ROOM",getFilesDir().getPath()+"/",roomID+".txt");
            replaceFileData(getFilesDir().getPath()+"/"+roomID+".txt",b);
            FLAG = 1;
        }
    };
    Runnable ftpDel = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            a.ftpDelete(ftpURL,"81","root","","/ROOM",roomID+".txt");
            FLAG = 1;
        }
    };
    Runnable ftpUpload = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            a.ftpUpload(ftpURL,"81","root","","/ROOM",getFilesDir().getPath()+"/",roomID+".txt");
            FLAG=1;
        }
    };


    public void read(View view) {

        EditText a = (EditText) findViewById(R.id.editText);
        TextView d =(TextView) findViewById(R.id.textView);
        TextView e =(TextView) findViewById(R.id.textView2);
        String text = String.valueOf(a.getText());
        String tex= "";
        String fileName = roomID+".txt";
        if(!text.isEmpty()) {
            new Thread(ftpDown).start();
            while (FLAG==0);
            FLAG=0;
            new Thread(ftpDel).start();
            while (FLAG==0);
            FLAG=0;
            writeFileData(getFilesDir().getPath()+"/"+fileName, myID);
            writeFileData(getFilesDir().getPath()+"/"+fileName, text);
            new Thread(ftpUpload).start();
            while (FLAG==0);
            writeFileData(fileName, myID);
            writeFileData(fileName, text);
            tex = readFileData(fileName);
        }
        String mine=""  , other="" ;
        tex = tex.substring(tex.indexOf("\r\n")+1,tex.length());
        d.setText(tex);
        while(true){
            if(tex.substring(0,tex.indexOf("\r\n")+1).contains(myID) ){
                mine = mine + tex.substring(0,tex.indexOf("\r\n"));
                tex = tex.substring(tex.indexOf("\r\n")+1,tex.length());
                if(tex.length()<10){mine+=tex;break;}
                else mine = mine + tex.substring(0,tex.indexOf("\r\n"));
                tex = tex.substring(tex.indexOf("\r\n")+1,tex.length());
                other = other +"\r\n\r\n";
            }else{
                other = other + tex.substring(0,tex.indexOf("\r\n"));
                tex = tex.substring(tex.indexOf("\r\n")+1,tex.length());
                if(tex.length()<10){other+=tex;break;}
                else other = other + tex.substring(0,tex.indexOf("\r\n"));
                tex = tex.substring(tex.indexOf("\r\n")+1,tex.length());
                mine = mine +"\r\n\r\n";
            }
        }
        d.setText(other);
        e.setText(mine);
    }


    public void Goback(View view) {
        String text = readFileData("roomInfo.txt");
        String tex="";
        /*删除自己在该聊天室的记录，并上传更新*/
        {
            while(true){
                if(!text.substring(0,text.indexOf("\r\n")+1).contains(myID)){
                    tex+=text.substring(0,text.indexOf("\r\n")+1);
                }
                if(text.length()<40)break;
                text =text.substring(text.indexOf("\r\n")+1,text.length());
            }
            tex+=text;
            new Thread(ftpDown).start();
            replaceFileData(getFilesDir().getPath()+"/"+"roomInfo.txt",tex);
            new Thread(ftpDel).start();
            new Thread(ftpUpload).start();
        }
        Intent intent = new Intent();
        intent.setClass(MainActivity.this,ChooseRoomActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",myinfo);
        intent.putExtras(bundle);
        this.startActivity(intent);
    }
}
