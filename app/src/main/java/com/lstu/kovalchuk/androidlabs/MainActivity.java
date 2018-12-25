package com.lstu.kovalchuk.androidlabs;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.lstu.kovalchuk.androidlabs.fragments.PRP.FragmentPRPLab1;
import com.lstu.kovalchuk.androidlabs.fragments.PRP.FragmentPRPLab3;
import com.lstu.kovalchuk.androidlabs.fragments.PRP.FragmentPRPLab4;
import com.lstu.kovalchuk.androidlabs.fragments.PRP.FragmentPRPLab5;
import com.lstu.kovalchuk.androidlabs.fragments.RMP.FragmentRMPLab1;
import com.lstu.kovalchuk.androidlabs.fragments.RMP.FragmentRMPLab2;
import com.lstu.kovalchuk.androidlabs.fragments.RMP.FragmentRMPLab3;
import com.lstu.kovalchuk.androidlabs.fragments.RMP.FragmentRMPLab45;
import com.lstu.kovalchuk.androidlabs.fragments.RMP.FragmentRMPLab6;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentPRPLab1 fgtPRPLab1;
    private FragmentPRPLab3 fgtPRPLab3;
    private FragmentPRPLab4 fgtPRPLab4;
    private FragmentPRPLab5 fgtPRPLab5;
    private FragmentRMPLab1 fgtRMPLab1;
    private FragmentRMPLab2 fgtRMPLab2;
    private FragmentRMPLab3 fgtRMPLab3;
    private FragmentRMPLab45 fgtRMPLab45;
    private FragmentRMPLab6 fgtRMPLab6;
    private TextView tvText;
    public Context applicationContext;
    public Context getApplicationContextForFragment(){
        return applicationContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        tvText = findViewById(R.id.mainText);
        fgtPRPLab1 = new FragmentPRPLab1();
        fgtPRPLab3 = new FragmentPRPLab3();
        fgtPRPLab4 = new FragmentPRPLab4();
        fgtPRPLab5 = new FragmentPRPLab5();
        fgtRMPLab1 = new FragmentRMPLab1();
        fgtRMPLab2 = new FragmentRMPLab2();
        fgtRMPLab3 = new FragmentRMPLab3();
        fgtRMPLab45 = new FragmentRMPLab45();
        fgtRMPLab6 = new FragmentRMPLab6();
        applicationContext = getApplicationContext();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            int count = getFragmentManager().getBackStackEntryCount();

            if (count == 0) {
                if (fgtRMPLab3.wvBrowser.canGoBack()) {
                    fgtRMPLab3.wvBrowser.goBack();
                } else {
                    super.onBackPressed();
                }
            } else {
                getFragmentManager().popBackStack();
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction ftrans = getSupportFragmentManager().beginTransaction();

        switch (id) {
            case R.id.nav_rmp_lab1:
                ftrans.replace(R.id.container, fgtRMPLab1);
                break;
            case R.id.nav_rmp_lab2:
                ftrans.replace(R.id.container, fgtRMPLab2);
                break;
            case R.id.nav_rmp_lab3:
                ftrans.replace(R.id.container, fgtRMPLab3);
                break;
            case R.id.nav_rmp_lab45:
                ftrans.replace(R.id.container, fgtRMPLab45);
                break;
            case R.id.nav_rmp_lab6:
                ftrans.replace(R.id.container, fgtRMPLab6);
                break;
            case R.id.nav_prp_lab1:
                ftrans.replace(R.id.container, fgtPRPLab1);
                break;
            case R.id.nav_prp_lab3:
                ftrans.replace(R.id.container, fgtPRPLab3);
                break;
            case R.id.nav_prp_lab4:
                ftrans.replace(R.id.container, fgtPRPLab4);
                break;
            case R.id.nav_prp_lab5:
                ftrans.replace(R.id.container, fgtPRPLab5);
                break;
        }

        ftrans.commit();
        tvText.setVisibility(View.GONE);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
