package in.umaenterprise.attendancemanagement.activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import in.umaenterprise.attendancemanagement.R;
import in.umaenterprise.attendancemanagement.model.AttendanceModel;
import in.umaenterprise.attendancemanagement.utils.CommonMethods;

public class ViewImagesActivity extends AppCompatActivity implements View.OnClickListener {
    AttendanceModel mAttendance;
    ImageView mImageView;
    TextView mButtonText,mHeadingText;
    int flag;
    ArrayList<String> imageList;
    ArrayList<String> titleList;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_images);
        init();
        setToolBar();
    }
    private void setToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(mAttendance.getPersonName());
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Drawable drawable = toolbar.getNavigationIcon();
        assert drawable != null;
        drawable.setColorFilter(ContextCompat.getColor(this, android.R.color.black), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    private void init(){
        Intent intent=this.getIntent();
        Bundle bundle=intent.getExtras();
        mAttendance=(AttendanceModel) bundle.getSerializable("attendanceModel");
        mImageView=findViewById(R.id.imageViewer);
        mButtonText=findViewById(R.id.textButton);
        mHeadingText=findViewById(R.id.headingText);
        imageList=new ArrayList<>();
        titleList=new ArrayList<>();
        titleList.add("Shift 1 Punch In Image");
        titleList.add("Shift 1 Punch Out Image");
        titleList.add("Shift 2 Punch In Image");
        titleList.add("Shift 2 Punch Out Image");
        if(!mAttendance.getPunchInImage().equals("")){
            imageList.add(mAttendance.getPunchInImage());
        }
        if(!mAttendance.getPunchOutImage().equals("")){
            imageList.add(mAttendance.getPunchOutImage());
        }
        if(!mAttendance.getPunchInImageS2().equals("")){
            imageList.add(mAttendance.getPunchInImageS2());
        }
        if(!mAttendance.getPunchOutImageS2().equals("")){
            imageList.add(mAttendance.getPunchOutImageS2());
        }
        if(imageList.size()>1){
            mButtonText.setVisibility(View.VISIBLE);
            mButtonText.setOnClickListener(this);
        }else {
            mButtonText.setVisibility(View.GONE);
        }
        setUpImageView(imageList.get(0),titleList.get(0));
        flag=0;

    }
    private void setUpImageView(String url,String title){
//        String url=mAttendance.getPunchInImage();
        CommonMethods.showProgressDialog(this);

        mImageView.setVisibility(View.INVISIBLE);
        mHeadingText.setVisibility(View.INVISIBLE);
        Picasso.with(this).load(url).into(mImageView, new Callback() {
            @Override
            public void onSuccess() {
                mHeadingText.setText(title);
                mImageView.setVisibility(View.VISIBLE);
                mHeadingText.setVisibility(View.VISIBLE);
                CommonMethods.cancelProgressDialog();
            }
            @Override
            public void onError() {
                CommonMethods.cancelProgressDialog();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.textButton:
                flag++;
                if(flag==imageList.size()){
                    flag=0;
                }
                setUpImageView(imageList.get(flag),titleList.get(flag));
                break;
        }
    }
}
