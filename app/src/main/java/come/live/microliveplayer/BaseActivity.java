package come.live.microliveplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Create By 刘胡来
 * Create Date 2019-12-14
 * Sensetime@Copyright
 * Des: 普通activity的基类
 */
public abstract class BaseActivity extends AppCompatActivity {

    private final int REQUEST_PERMISSION_CODE = 0x0021;
    protected String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
    }

    protected abstract int getLayout();


    /**
     * 申请权限
     * @param permissions
     */
    protected void requestPermission(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION_CODE);
    }

    /**
     * 判断是否拥有某个权限
     * @param permissions
     * @return
     */
    protected boolean hasPermission(String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        boolean isGranted = true;
        if(requestCode == REQUEST_PERMISSION_CODE){
            for (int i = 0; i < grantResults.length; i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    onRejectPermissions();
                    isGranted = false;
                    break;
                }
            }
            if(isGranted){
                onHoldPermissions();
            }
        }
    }

    /**
     * 权限拒绝回调
     */
    protected void onRejectPermissions(){}

    /**
     * 获得所有权限
     */
    protected void onHoldPermissions(){}


}
