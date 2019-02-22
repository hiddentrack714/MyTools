package com.track.mytools.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.client.android.CaptureActivity;
import com.track.mytools.R;
import com.track.mytools.util.QRCodeUtil;
import com.track.mytools.util.ToolsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

/**
 * 生成和解析二维码
 */
public class QRCodeActivity extends Activity {

    @BindView(R.id.qrContent)
    EditText qrContent;

    @BindView(R.id.qrGenBtn)
    Button qrGenBtn;

    @BindView(R.id.qrAnaBtn)
    Button qrAnaBtn;

    @BindView(R.id.qrScanBtn)
    Button qrScanBtn;

    @BindView(R.id.qrImg)
    ImageView qrImg;

    @BindView(R.id.qrSaveBtn)
    Button qrSaveBtn;

    private final int EX_FILE_PICKER_RESULT = 0xfa01;
    private String startDirectory = null;// 记忆上一次访问的文件目录路径

    private Bitmap bitmap = null;

    private static int REQUEST_CODE_SCAN = 0x0000;

    private static int REQUEST_CODE_CAMERA = 0x0011;

    private static final String DECODED_CONTENT_KEY = "codedContent";
    private static final String DECODED_BITMAP_KEY = "codedBitmap";

    private boolean flag;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode);
        ButterKnife.bind(this);

        //监听生成二维码按钮
        qrGenBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String qrContentStr =  qrContent.getText().toString();
                if("".equals(qrContentStr)){
                    ToolsUtil.showToast(QRCodeActivity.this,"请输入二维码内容",2000);
                    return;
                }

                final AlertDialog.Builder normalDialog =
                        new AlertDialog.Builder(QRCodeActivity.this);
                normalDialog.setTitle("选项");
                normalDialog.setMessage("是否要添加中心Logo?");
                normalDialog.setPositiveButton("是,并且选择Logo位置",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                flag = true;

                                ExFilePicker exFilePicker = new ExFilePicker();
                                exFilePicker.setCanChooseOnlyOneItem(true);// 单选
                                exFilePicker.setQuitButtonEnabled(true);
                                exFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);

                                if (TextUtils.isEmpty(startDirectory)) {
                                    exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                                } else {
                                    exFilePicker.setStartDirectory(startDirectory);
                                }

                                exFilePicker.start(QRCodeActivity.this, EX_FILE_PICKER_RESULT);

                                qrSaveBtn.setEnabled(true);

                            }
                        });
                normalDialog.setNegativeButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                bitmap = QRCodeUtil.createQRImage(qrContentStr, 230, 230, null, null);

                                qrImg.setImageBitmap(bitmap);

                                qrSaveBtn.setEnabled(true);
                            }
                        });
                // 显示
                normalDialog.show();
            }
        });

        //监听解析二维码按钮
        qrAnaBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                flag = false;

                ExFilePicker exFilePicker = new ExFilePicker();
                exFilePicker.setCanChooseOnlyOneItem(true);// 单选
                exFilePicker.setQuitButtonEnabled(true);
                exFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);

                if (TextUtils.isEmpty(startDirectory)) {
                    exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                } else {
                    exFilePicker.setStartDirectory(startDirectory);
                }

                exFilePicker.start(QRCodeActivity.this, EX_FILE_PICKER_RESULT);

                qrSaveBtn.setEnabled(true);
            }
        });

        //监听扫描二维码按钮
        qrScanBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                //动态权限申请
                if (ContextCompat.checkSelfPermission(QRCodeActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(QRCodeActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_CAMERA);
                } else {
                    //扫码
                    goScan();
                }
            }
        });

        //监听保存二维码按钮
        qrSaveBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                ExFilePicker exFilePicker = new ExFilePicker();
                exFilePicker.setCanChooseOnlyOneItem(true);// 单选
                exFilePicker.setQuitButtonEnabled(true);
                exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);

                if (TextUtils.isEmpty(startDirectory)) {
                    exFilePicker.setStartDirectory(Environment.getExternalStorageDirectory().getPath());
                } else {
                    exFilePicker.setStartDirectory(startDirectory);
                }

                exFilePicker.start(QRCodeActivity.this, EX_FILE_PICKER_RESULT);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String path = result.getPath();

                List<String> names = result.getNames();
                for (int i = 0; i < names.size(); i++) {
                    File f = new File(path, names.get(i));
                    try {
                        Uri uri = Uri.fromFile(f); //这里获取了真实可用的文件资源
                        Log.i("QRCodeActivity_Log","二维码地址:" + uri.getPath());

                        if(uri.getPath().indexOf(".")>-1){
                            //选择二维码文件
                            //1，选择中心Logo
                            //2，解析二维码

                            if(flag){
                                bitmap = QRCodeUtil.createQRImage(qrContent.getText().toString(), 230, 230, null, null);

                                Bitmap logoBitmap = BitmapFactory.decodeFile(uri.getPath());
                                //加灰色边框
                                logoBitmap = QRCodeUtil.whiteEdgeBitmap(logoBitmap,15, Color.GRAY);
                                //改圆角
                                logoBitmap = QRCodeUtil.getRoundedCornerBitmap(logoBitmap,15);
                                //加白色边框
                                logoBitmap = QRCodeUtil.whiteEdgeBitmap(logoBitmap,30, Color.WHITE);
                                //改圆角
                                logoBitmap = QRCodeUtil.getRoundedCornerBitmap(logoBitmap,15);

                                bitmap = QRCodeUtil.addLogo(bitmap,logoBitmap);

                                qrImg.setImageBitmap(bitmap);

                            }else{
                                Result res = QRCodeUtil.analysisQRImage(uri.getPath());

                                qrImg.setImageBitmap(BitmapFactory.decodeFile(uri.getPath(),QRCodeUtil.getBitmapOption(2)));

                                qrContent.setText(res.getText());
                            }
                        }else{
                            //保存二维码
                            if(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(uri.getPath()+"/code.jpg"))){
                                ToolsUtil.showToast(QRCodeActivity.this,"二维码保存成功",2000);
                            }else{
                                ToolsUtil.showToast(QRCodeActivity.this,"二维码保存失败",2000);
                            }

                        }

                        startDirectory = path;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else if(requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK){
            if (data != null) {
                //返回的文本内容
                String content = data.getStringExtra(DECODED_CONTENT_KEY);
                //返回的BitMap图像
                Bitmap bitmap = data.getParcelableExtra(DECODED_BITMAP_KEY);

                qrContent.setText(content);

                qrImg.setImageBitmap(bitmap);
                //旋转图片
                qrImg.setPivotX(qrImg.getWidth()/2);
                qrImg.setPivotY(qrImg.getHeight()/2);//支点在图片中心
                qrImg.setRotation(90);

                qrSaveBtn.setEnabled(true);
            }
        }
    }

    /**
     * 跳转到扫码界面扫码
     */
    private void goScan(){
        Intent intent = new Intent(QRCodeActivity.this, CaptureActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SCAN);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0x0011:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //扫码
                    goScan();
                } else {
                    Toast.makeText(this, "你拒绝了权限申请，无法打开相机扫码哟！", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
