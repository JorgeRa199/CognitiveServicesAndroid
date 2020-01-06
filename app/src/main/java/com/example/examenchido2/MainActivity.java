package com.example.examenchido2;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import com.example.examenchido2.FrmtContact;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ViewPageAdapter viewPageAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout_id);
        viewPager = (ViewPager) findViewById(R.id.pageViewId);

        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        //     Add Fragment
        viewPageAdapter.AddFrmt(new Frmt_call(),"");
        viewPageAdapter.AddFrmt(new FrmtContact(),"");
        viewPageAdapter.AddFrmt(new Frmt_fav(),"");


        viewPager.setAdapter(viewPageAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_note_add_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_book_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_favorite_black_24dp);


        //Remove ActionBar Shadow

        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
    }


    public void activityAnalyze(View v) {
        Intent intent = new Intent(this, AnalyzeActivity.class);
        startActivity(intent);
    }

    public void activityAnalyzeInDomain(View v) {
        Intent intent = new Intent(this, AnalyzeInDomainActivity.class);
        startActivity(intent);
    }

    public void activityDescribe(View v) {
        Intent intent = new Intent(this, DescribeActivity.class);
        startActivity(intent);
    }

    public void activityHandwriting(View v)
    {
        Intent intent =new Intent(this, HandwritingRecognizeActivity.class);
        startActivity(intent);
    }

    public void activityRecognize(View v) {
        Intent intent = new Intent(this, RecognizeActivity.class);
        startActivity(intent);
    }



}
