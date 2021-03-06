package ca.coffeeshopstudio.gaminginterfaceclient.views;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import ca.coffeeshopstudio.gaminginterfaceclient.R;
import ca.coffeeshopstudio.gaminginterfaceclient.models.FontCache;
import ca.coffeeshopstudio.gaminginterfaceclient.models.GICControl;
import ca.coffeeshopstudio.gaminginterfaceclient.models.screen.IScreen;
import ca.coffeeshopstudio.gaminginterfaceclient.models.screen.IScreenRepository;
import ca.coffeeshopstudio.gaminginterfaceclient.models.screen.Screen;
import ca.coffeeshopstudio.gaminginterfaceclient.models.screen.ScreenRepository;

/**
 Copyright [2019] [Terence Doerksen]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
public abstract class AbstractGameActivity extends AppCompatActivity implements View.OnClickListener {
    protected IScreen currentScreen;
    protected int currentApiVersion;
    protected int currentScreenId;

    protected IScreenRepository screenRepository;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null)
            currentScreenId = getIntent().getIntExtra(MainActivity.INTENT_SCREEN_INDEX, 0);

        screenRepository = new ScreenRepository(getApplicationContext());
        screenRepository.loadScreens();
        currentScreen = screenRepository.getScreen(currentScreenId);

        buildFontCache();
    }

    private void buildFontCache() {
        FontCache.buildCache(this);
    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    protected StateListDrawable buildButtonDrawable(GICControl control) {
        Drawable primary;
        Drawable secondary;

        if (control.getPrimaryColor() != -1) {
            //color gradients
            //we don't support mixing color and otherwise
            //so if secondary is -1, make it match primary (replicate 1.3 bug)
            if (control.getSecondaryColor() == -1)
                control.setSecondaryColor(control.getPrimaryColor());
            primary = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{control.getSecondaryColor(), control.getPrimaryColor()});
            ((GradientDrawable) primary).setCornerRadius(3f);
        } else if (!control.getPrimaryImage().isEmpty()) {
            //build drawable based on path
            primary = Drawable.createFromPath(control.getPrimaryImage());
        } else if (control.getPrimaryImageResource() != -1) {
            //build based on built in resource
            primary = getButtonResource(control.getPrimaryImageResource(), control.getViewType(), true);
        } else { //fallback
            //color gradients
            primary = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.BLACK, Color.WHITE});
            ((GradientDrawable) primary).setCornerRadius(3f);
        }

        if (control.getSecondaryColor() != -1) {
            //color gradients
            secondary = new GradientDrawable(
                    GradientDrawable.Orientation.BOTTOM_TOP,
                    new int[]{control.getPrimaryColor(), control.getSecondaryColor()});
            ((GradientDrawable) secondary).setCornerRadius(3f);
        } else if (!control.getSecondaryImage().isEmpty()) {
            //build drawable based on path
            secondary = Drawable.createFromPath(control.getSecondaryImage());
        } else if (control.getSecondaryImageResource() != -1) {
            //build based on built in resource
            secondary = getButtonResource(control.getSecondaryImageResource(), control.getViewType(), false);
        } else { //fallback
            //color gradients
            secondary = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{Color.WHITE, Color.BLACK});
            ((GradientDrawable) secondary).setCornerRadius(3f);
        }

        StateListDrawable res = new StateListDrawable();
        if (control.getViewType() == GICControl.TYPE_BUTTON) {
            res.addState(new int[]{android.R.attr.state_pressed}, secondary);
        } else {
            res.addState(new int[]{android.R.attr.state_checked}, secondary);
        }
        res.addState(new int[]{}, primary);
        return res;
    }

    //primary is used for backwards compatability
    private Drawable getButtonResource(int resourceId, int type, boolean primary) {
        if (type == GICControl.TYPE_BUTTON) {
            switch (resourceId) {
                case 0:
                    return getResources().getDrawable(R.drawable.button_neon);
                case 1:
                    return getResources().getDrawable(R.drawable.button_neon_pushed);
                case 2:
                    return getResources().getDrawable(R.drawable.button_blue);
                case 3:
                    return getResources().getDrawable(R.drawable.button_blue_dark);
                case 4:
                    return getResources().getDrawable(R.drawable.button_green);
                case 5:
                    return getResources().getDrawable(R.drawable.button_green_dark);
                case 6:
                    return getResources().getDrawable(R.drawable.button_green_alt);
                case 7:
                    return getResources().getDrawable(R.drawable.button_green_alt_dark);
                case 8:
                    return getResources().getDrawable(R.drawable.button_purple);
                case 9:
                    return getResources().getDrawable(R.drawable.button_purple_dark);
                case 10:
                    return getResources().getDrawable(R.drawable.button_red);
                case 11:
                    return getResources().getDrawable(R.drawable.button_red_dark);
                case 12:
                    return getResources().getDrawable(R.drawable.button_yellow);
                case 13:
                    return getResources().getDrawable(R.drawable.button_yellow_dark);
                default:
                    if (primary)
                        return getResources().getDrawable(R.drawable.button_neon);
                    else
                        return getResources().getDrawable(R.drawable.button_neon_pushed);
            }
        } else {
            switch (resourceId) {
                case 0:
                    return getResources().getDrawable(R.drawable.switch_off);
                case 1:
                    return getResources().getDrawable(R.drawable.switch_on);
                default:
                    if (primary)
                        return getResources().getDrawable(R.drawable.switch_off);
                    else
                        return getResources().getDrawable(R.drawable.switch_on);
            }
        }
    }

    protected void addDragDrop(View view) {
        //unused
    }

    protected void loadScreen() {
        for (GICControl control : currentScreen.getControls()) {
            switch (control.getViewType()) {
                case 0:
                    buildButton(control);
                    break;
                case 1:
                    buildText(control);
                    break;
                case 2:
                    buildImage(control);
                    break;
                case 3:
                    buildSwitch(control);
            }
        }

        View topLayout = findViewById(R.id.topLayout);
        Drawable background = currentScreen.getBackground();
        topLayout.setBackground(background);
    }

    protected View buildText(GICControl control) {
        AppCompatTextView view = new AppCompatTextView(AbstractGameActivity.this);
        TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(view, 24, Screen.MAX_CONTROL_SIZE, 2, TypedValue.COMPLEX_UNIT_SP);
        buildView(control, view);
        initText(view, control);
        return view;
    }

    protected View buildButton(GICControl control) {
        Button view = new Button(AbstractGameActivity.this);
        view.setBackground(buildButtonDrawable(control));
        initText(view, control);
        buildView(control, view);
        return view;
    }

    protected View buildSwitch(GICControl control) {
        ToggleButton view = new ToggleButton(AbstractGameActivity.this);

        view.setBackground(buildButtonDrawable(control));
        initText(view, control);
        view.setTextOff(view.getText());
        view.setTextOn(view.getText());
        buildView(control, view);
        return view;
    }

    protected View buildImage(GICControl control) {
        ImageView view = new ImageView(AbstractGameActivity.this);
        buildView(control, view);
        if (control.getPrimaryImage().isEmpty()) {
            view.setImageResource(R.mipmap.ic_launcher);
            resizeImageView(view, control.getWidth(), control.getHeight());
        }
        else {
            Drawable image = currentScreen.getImage(control.getPrimaryImage());
            view.setImageDrawable(image);
            resizeImageView(view, control.getWidth(), control.getHeight());
        }

        return view;
    }

    //init generic to all view types
    private void buildView(GICControl control, View view) {
        FrameLayout layout = findViewById(R.id.topLayout);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(control.getWidth(), control.getHeight());
        layout.addView(view, lp);

        view.setX(control.getLeft());
        view.setY(control.getTop());

        view.setTag(control);
        //currentScreen.addControl(control);

        setClick(view);

        addDragDrop(view);

        view.setId(currentScreen.getNewControlId());
    }

    //initializations related to textview based controls (AppCompatTextView, Button, etc)
    private void initText(TextView view, GICControl control) {
        view.setWidth(control.getWidth());
        view.setHeight(control.getHeight());
        view.setTextColor(control.getFontColor());

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, control.getFontSize());
        view.setText(control.getText());
        setFontTypeface(view, control);
    }

    protected void setFontTypeface(TextView textView, GICControl control) {
        if (control.getFontName().isEmpty()) {
            textView.setTypeface(Typeface.DEFAULT);
        } else {
            if (control.getFontType() == 0) {
                textView.setTypeface(FontCache.get(control.getFontName(), this));
            } else {
                textView.setTypeface(FontCache.get(control.getFontName(), this));
            }
        }
    }

    protected void setClick(View view) {
        view.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

    }

    protected void setupFullScreen() {
        currentApiVersion = Build.VERSION.SDK_INT;
        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(flags);
            final View decorView = getWindow().getDecorView();
            decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        decorView.setSystemUiVisibility(flags);
                    }
                }
            });
        }
    }

    protected void resizeImageView(ImageView view, int newWidth, int newHeight) {
        FrameLayout.LayoutParams layout = new FrameLayout.LayoutParams(newWidth, newHeight);
        view.setLayoutParams(layout);
        view.invalidate();
    }
}
