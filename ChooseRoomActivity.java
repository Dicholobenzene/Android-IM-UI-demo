package aria.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import static android.os.Build.ID;

public class ChooseRoomActivity extends AppCompatActivity {
    ftpMethods.user info;
    int FLAG = 0;
    String filename ="roomInfo.txt";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_room);
        Intent intent = this.getIntent();
        info =(ftpMethods.user) intent.getSerializableExtra("user");
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
            String b =a.ftpDownload(ftpURL,"81","root","","/ROOM",getFilesDir().getPath()+"/","roomInfo.txt");
            replaceFileData(getFilesDir().getPath()+"/roomInfo.txt",b);
            FLAG = 1;
        }
    };
    Runnable ftpDel = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            a.ftpDelete(ftpURL,"81","root","","/ROOM","roomInfo.txt");
            FLAG = 1;
        }
    };
    Runnable ftpUpload = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            a.ftpUpload(ftpURL,"81","root","","/ROOM",getFilesDir().getPath()+"/","roomInfo.txt");
            FLAG=1;
        }
    };

    public void EnterRoom(View view) {
        EditText ROOM = (EditText)findViewById(R.id.RoomEdit);
        int roomid = Integer.parseInt(String.valueOf(ROOM.getText()));
        info.setRoom(roomid);
        Intent intent = new Intent();
        intent.setClass(ChooseRoomActivity.this,MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",info);
        intent.putExtras(bundle);
        /*向聊天室里增加一条新记录*/
        {
            new Thread(ftpDown).start();
            while (FLAG==0);
            FLAG=0;
            new Thread(ftpDel).start();
            while (FLAG==0);
            FLAG=0;
            writeFileData(getFilesDir().getPath()+"/"+filename,String.valueOf(ROOM.getText()));
            writeFileData(getFilesDir().getPath()+"/"+filename,info.getID()+" "+info.getJob()+" "+info.getHobby()+" "+info.getLatitude()+" "+info.getLongitude());
            new Thread(ftpUpload).start();
            while (FLAG==0);
        }
        writeFileData(getFilesDir().getPath()+"/"+filename,String.valueOf(ROOM.getText()));
        writeFileData(getFilesDir().getPath()+"/"+filename,info.getID()+" "+info.getJob()+" "+info.getHobby()+" "+info.getLatitude()+" "+info.getLongitude());
        this.startActivity(intent);
    }

    public void Search(View view) {
        EditText cha= (EditText) findViewById(R.id.SerachEdit);
        String character = String.valueOf(cha.getText());
        Intent intent = new Intent();
        intent.setClass(ChooseRoomActivity.this,SearchUserActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("key",character);
        bundle.putSerializable("user",info);
        intent.putExtras(bundle);
        this.startActivity(intent);
    }

    public void SearchRoom(View view) {
        String text,tex=" ";
        text = readFileData(filename);
        EditText roomID = (EditText) findViewById(R.id.RoomEdit);
        String roomid = String.valueOf(roomID.getText());
        while(true){
            if(text.substring(0,4).contains(roomid)){
                text =text.substring(text.indexOf("\r\n")+1,text.length());
                tex+=text.substring(0,text.indexOf("\r\n")+1);
            }
            if(text.length()<40)break;
            text =text.substring(text.indexOf("\r\n")+1,text.length());
        }
        TextView roomIF = (TextView) findViewById(R.id.RoomChar);
        roomIF.setText(tex);
    }
}
