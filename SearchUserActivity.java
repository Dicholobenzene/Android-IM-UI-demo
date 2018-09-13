package aria.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class SearchUserActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);
        TextView a = (TextView) findViewById(R.id.textView3);
        TextView b = (TextView) findViewById(R.id.textView8);
        String chara = (String) this.getIntent().getSerializableExtra("key");
        String text = readFileData("userInfo.txt"),tex = "";
        a.setText("搜索关键词:"+chara);
        b.setText(text);
        for(int i =0;i<200;i++){
            if(text.substring(0,text.indexOf("\r\n")+1).contains(chara)){
                tex+=text.substring(0,text.indexOf("\r\n")+1);
            }
            if(text.indexOf("\r\n")>=text.length())break;
            text =text.substring(text.indexOf("\r\n")+1,text.length());
        }
        b.setText(tex);
    }

    public void goBack(View view) {
        ftpMethods.user a = (ftpMethods.user) this.getIntent().getSerializableExtra("user");
        Intent intent = new Intent();
        intent.setClass(SearchUserActivity.this,ChooseRoomActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",a);
        intent.putExtras(bundle);
        this.startActivity(intent);
    }
}
