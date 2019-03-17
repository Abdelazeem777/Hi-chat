package com.ahk.sayhi;

import android.content.Intent;

import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

public class welcomeActivity extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimary)
                .page(new TitlePage(R.drawable.sayhilogo,"\n\n\nWelcome\n")
                )
                .swipeToDismiss(false)
                .bottomLayout(WelcomeConfiguration.BottomLayout.BUTTON_BAR)
                .canSkip(false)
                .backButtonSkips(false)
                .showActionBarBackButton(false)
                .backButtonNavigatesPages(false)
                .build();
    }

    @Override
    protected void onButtonBarFirstPressed() {
        Intent n=new Intent(getApplicationContext(),loginActivity.class);
        startActivity(n);
    }

    @Override
    protected void onButtonBarSecondPressed() {
        Intent n=new Intent(getApplicationContext(),registerationActivity.class);
        startActivity(n);
    }
}
