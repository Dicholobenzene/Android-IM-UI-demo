package aria.myapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.http.util.EncodingUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class LoginActivity extends AppCompatActivity {

    String id, job, hobby, latitude = "0", longitude = "0";
    int FLAG = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    String ftpURL = "192.168.3.29";
    String ftpPort ="81";

    Runnable ftpDown = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            String b = a.ftpDownload(ftpURL, "81", "root", "", "/ROOM", getFilesDir().getPath() + "/", "userInfo.txt");
            FLAG = 1;
        }
    };
    Runnable ftpDel = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            a.ftpDelete(ftpURL, "81", "root", "", "/ROOM", "userInfo.txt");
            FLAG = 1;
        }
    };
    Runnable ftpUpload = new Runnable() {
        @Override
        public void run() {
            ftpMethods a = new ftpMethods();
            try {
//                a.ftpStreamUpload("sducph.cn","21","kopiko","0","/ROOM","userInfo.txt",openFileInput("userInfo.txt"));
                a.ftpStreamUpload(ftpURL, "81", "root", "", "/ROOM", "userInfo.txt", openFileInput("userInfo.txt"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            FLAG = 1;
        }
    };

    boolean findGPS() {
        LocationManager alm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) return true;
        return false;
    }

    public int writeFileData(String fileName, String message) {
        try {
            byte[] c = new byte[2];
            c[0] = 0x0d;
            c[1] = 0x0a;//用于输入换行符的字节码
            String old = readFileData(fileName);
            FileOutputStream fout = openFileOutput(fileName, MODE_PRIVATE);
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


    public void Login(View view) throws IOException, InterruptedException {
        EditText ID = (EditText) findViewById(R.id.IDtext);
        id = String.valueOf(ID.getText());
        EditText JOB = (EditText) findViewById(R.id.JOBtext);
        job = String.valueOf(JOB.getText());
        EditText HOBBY = (EditText) findViewById(R.id.HOBBYtext);
        hobby = String.valueOf(HOBBY.getText());
        if (!findGPS()) {
            LocationManager loc;
            String serName = Context.LOCATION_SERVICE;
            loc = (LocationManager) this.getSystemService(serName);
            Criteria cri = new Criteria();
            cri.setAccuracy(Criteria.ACCURACY_FINE);
            cri.setAltitudeRequired(false);
            cri.setBearingRequired(false);
            cri.setCostAllowed(true);
            cri.setPowerRequirement(Criteria.POWER_MEDIUM);
            String pro = loc.getBestProvider(cri, true);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location LOC = loc.getLastKnownLocation(pro);
            latitude = String.valueOf(LOC.getLatitude());
            longitude = String.valueOf(LOC.getLongitude());
        }
        ftpMethods.user information = new ftpMethods.user(id,job,hobby,latitude,longitude);
        /*用来上传用户信息*/
        {
            new Thread(ftpDown).start();
            while (FLAG==0);
            FLAG=0;
            new Thread(ftpDel).start();
            while (FLAG==0);
            FLAG=0;
            writeFileData("userInfo.txt", "ID:" + id + " JOB:" + job + " HOBBY:" + hobby + " LATITUDE:" + latitude+ " LONGITUDE:" +longitude);
            new Thread(ftpUpload).start();
            while (FLAG==0);
        }
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this,ChooseRoomActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("user",information);
        intent.putExtras(bundle);
        this.startActivity(intent);
    }
}
