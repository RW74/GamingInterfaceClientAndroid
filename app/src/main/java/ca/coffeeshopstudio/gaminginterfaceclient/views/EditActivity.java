package ca.coffeeshopstudio.gaminginterfaceclient.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import ca.coffeeshopstudio.gaminginterfaceclient.R;
import ca.coffeeshopstudio.gaminginterfaceclient.models.Command;
import ca.coffeeshopstudio.gaminginterfaceclient.models.Control;

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
public class EditActivity extends AbstractGameActivity implements EditFragment.EditDialogListener, SeekBar.OnSeekBarChangeListener, EditBackgroundFragment.EditDialogListener {
    private GestureDetector gd;
    private SeekBar width;
    private SeekBar height;
    private SeekBar fontSize;
    private boolean mode = false;
    private final int minControlSize = 48;
    private final int maxFontSize = 256;
    private ControlTypes controlTypes;

    protected static final int OPEN_REQUEST_CODE_BACKGROUND = 41;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        controlTypes = new ControlTypes(getApplicationContext());

        setContentView(R.layout.activity_edit);
        setupFullScreen();
        setupDoubleTap(EditActivity.this);
        setupControls();
        loadControls();
        toggleEditControls(View.GONE);
    }

    private View findControl(int id) {
        for (View view : views) {
            if (view.getId() == id)
                return view;
        }
        return null;
    }

    private void toggleEditControls(int visibility) {
        if (activeControl >= 0) {
            View view = findControl(activeControl);
            if (view instanceof Button) {
                findViewById(R.id.seekFontSize).setVisibility(visibility);
            } else {
                findViewById(R.id.seekFontSize).setVisibility(View.GONE);
            }
            findViewById(R.id.seekHeight).setVisibility(visibility);
            findViewById(R.id.seekWidth).setVisibility(visibility);
        }
    }

    private void setupControls() {
        width = findViewById(R.id.seekWidth);
        height = findViewById(R.id.seekHeight);
        fontSize = findViewById(R.id.seekFontSize);

        findViewById(R.id.topLayout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.performClick();
                return gd.onTouchEvent(event);
            }
        });
        findViewById(R.id.topLayout).setOnDragListener(new DragDropListener());

        ((Switch) findViewById(R.id.toggleMode)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mode = b;
                if (mode) {
                    toggleEditControls(View.GONE);
                    Toast.makeText(EditActivity.this, R.string.edit_activity_drag_mode, Toast.LENGTH_SHORT).show();
                } else if (activeControl > -1) {
                    toggleEditControls(View.VISIBLE);
                    Toast.makeText(EditActivity.this, R.string.edit_activity_detail_edit_mode, Toast.LENGTH_SHORT).show();
                }
            }
        });

        width.setMax(maxControlSize);
        height.setMax(maxControlSize);
        fontSize.setMax(maxFontSize);
        width.setOnSeekBarChangeListener(this);
        height.setOnSeekBarChangeListener(this);
        fontSize.setOnSeekBarChangeListener(this);

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveScreen();
            }
        });

        findViewById(R.id.btnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayEditBackgroundDialog();
            }
        });
    }

    private void saveScreen() {
        View topLayout = findViewById(R.id.topLayout);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("gicsScreen", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        //save the images
        if (topLayout.getBackground() instanceof ColorDrawable) {
            ColorDrawable color = (ColorDrawable) topLayout.getBackground();
            prefsEditor.putInt("background", color.getColor());
        } else {
            BitmapDrawable bitmap = (BitmapDrawable) topLayout.getBackground();
            saveBitmapIntoSDCardImage(bitmap.getBitmap());
            prefsEditor.putInt("background", -1);
        }

        ObjectMapper mapper = new ObjectMapper();

        //first we need to remove all existing views
        Map<String,?> keys = prefs.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            if (entry.getKey().contains("control_")) {
                prefsEditor.remove(entry.getKey());
            }
        }

        try {
            int i = 0;
            for (View view : views) {
                Control control = new Control();
                control.setCommand((Command) view.getTag());
                control.setWidth(view.getWidth());
                control.setLeft(view.getX());
                control.setFontSize((int) ((TextView) view).getTextSize());
                control.setText(((TextView) view).getText().toString());
                control.setTop(view.getY());
                control.setHeight(view.getBottom());
                control.setFontColor(((TextView) view).getTextColors().getDefaultColor());
                control.setPrimaryColor(primaryColors.get(i));
                control.setSecondaryColor(secondaryColors.get(i));
                if (view instanceof Button)
                    control.setViewType(0);
                else
                    control.setViewType(1);
                String json = mapper.writeValueAsString(control);
                prefsEditor.putString("control_" + i, json);
                i++;
            }
            prefsEditor.apply();
            Toast.makeText(EditActivity.this, R.string.edit_activity_saved, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(EditActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupDoubleTap(final Context context) {
        gd = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            //here is the method for double tap
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                showControlPopup();
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    private void showControlPopup() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(EditActivity.this);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(EditActivity.this, android.R.layout.simple_list_item_1, controlTypes.getStringValues());

        builderSingle.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (controlTypes.getValue(which) == getString(R.string.control_type_button))
                    addButton();
                if (controlTypes.getValue(which) == getString(R.string.control_type_text))
                    addTextView();
                if (controlTypes.getValue(which) == getString(R.string.control_type_image))
                    addImage();
            }
        });
        builderSingle.show();
    }

    private void addImage() {
        Control control = initNewControl();

        buildImage(control);

        updateDisplay();
    }

    private void addTextView() {
        //un select any previous button
        Control control = initNewControl();
        control.setText(getString(R.string.default_control_text));

        buildText(control);

        updateDisplay();
    }

    private void addButton() {
        //unselect any previous button
        Control control = initNewControl();
        control.setText(getString(R.string.default_control_text));

        buildButton(control);

        View view = updateDisplay();

        ((Button) view).setTextSize(TypedValue.COMPLEX_UNIT_PX, 48);
        fontSize.setProgress((int) ((Button) view).getTextSize());
    }

    private Control initNewControl() {
        //un select any previous view visually
        unselectedPreviousView();
        Control control = new Control();
        width.setProgress(control.getWidth());
        height.setProgress(control.getHeight());

        return control;
    }

    private void unselectedPreviousView() {
//        if (activeControl >= 0) {
//            View previous = findViewById(views.get(activeControl).getId());
//            if (previous instanceof Button) {
//                previous.setBackground(setButtonBackground(primaryColors.get(activeControl), secondaryColors.get(activeControl)));
//            }
//        }
    }

    //called after addControlType style methods
    @SuppressLint("ClickableViewAccessibility")
    private View updateDisplay() {
        View view = views.get(views.size() - 1);
        view.setOnClickListener(this);
        view.setOnTouchListener(new TouchListener());
        activeControl = views.size() - 1;
        toggleEditControls(View.VISIBLE);

        return view;
    }

    private void displayEditBackgroundDialog() {
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
//        startActivityForResult(intent, OPEN_REQUEST_CODE_BACKGROUND);
        FragmentManager fm = getSupportFragmentManager();

        int color = Color.BLACK;
        Drawable background = findViewById(R.id.topLayout).getBackground();
        if (background instanceof ColorDrawable)
            color = ((ColorDrawable) background).getColor();

        int primaryColor = color;
        //int secondaryColor = secondaryColors.get(activeControl);

        EditBackgroundFragment editNameDialogFragment = EditBackgroundFragment.newInstance(getString(R.string.title_fragment_edit), primaryColor);
        editNameDialogFragment.show(fm, "fragment_edit_background_name");
    }

    private void displayTextEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        TextView view = (TextView) findControl(activeControl);
        int fontColor = view.getTextColors().getDefaultColor();
        int primaryColor = primaryColors.get(activeControl);
        int secondaryColor = secondaryColors.get(activeControl);
        String buttonText = (String) view.getText();
        Command commandToSend = ((Command) findControl(activeControl).getTag());
        EditFragment editNameDialogFragment = EditFragment.newInstance(getString(R.string.title_fragment_edit), buttonText, commandToSend, primaryColor, secondaryColor, fontColor, view);
        editNameDialogFragment.show(fm, "fragment_edit_name");
    }

    @Override
    protected void addDragDrop(View view) {
        view.setOnTouchListener(new TouchListener());
    }

    @Override
    public void onClick(View view) {
        if (activeControl == view.getId()) {
            if (view instanceof TextView)
                displayTextEditDialog();
            if (view instanceof ImageView)
                displayImageEditDialog();
        } else {
            if (activeControl >= 0) {
                if (findControl(activeControl) instanceof Button)
                    findControl(activeControl).setBackground(setButtonBackground(primaryColors.get(activeControl), secondaryColors.get(activeControl)));
            }
            activeControl = view.getId();

            if (view instanceof Button)
                view.setBackground(setButtonBackground(secondaryColors.get(activeControl), primaryColors.get(activeControl)));

            width.setProgress(view.getWidth());
            height.setProgress(view.getHeight());
            fontSize.setProgress((int) ((TextView) view).getTextSize());
            toggleEditControls(View.VISIBLE);
        }
    }

    private void displayImageEditDialog() {
    }

    @Override
    public void onFinishEditBackgroundDialog(int primaryColor, Uri image) {
        if (image == null)
            findViewById(R.id.topLayout).setBackgroundColor(primaryColor);
        else {
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), image);
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                findViewById(R.id.topLayout).setBackground(drawable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFinishEditDialog(Command command, String text, int primaryColor, int secondaryColor, int fontColor) {
        if (command == null && text.equals("DELETE")) {
            if (activeControl >= 0) {
                FrameLayout layout = findViewById(R.id.topLayout);
                layout.removeView(findControl(activeControl));
                views.remove(findControl(activeControl));
                //primaryColors.remove(activeControl);
                //secondaryColors.remove(activeControl);
                activeControl = -1;
                toggleEditControls(View.GONE);
            }
        } else {
            primaryColors.set(activeControl, primaryColor);
            secondaryColors.set(activeControl, secondaryColor);

            View view = findControl(activeControl);

            if (view instanceof Button)
                view.setBackground(setButtonBackground(primaryColor, secondaryColor));

            ((TextView) view).setText(text);
            ((TextView) view).setTextColor(fontColor);
            view.setTag(command);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        if (activeControl >= 0) {
            TextView view = (TextView) findControl(activeControl);

            int newWidth = view.getWidth();
            int newHeight = view.getHeight();
            int newFont = (int) view.getTextSize();
            switch (seekBar.getId()) {
                case R.id.seekHeight:
                    newHeight = value;
                    break;
                case R.id.seekWidth:
                    newWidth = value;
                    break;
                case R.id.seekFontSize:
                    newFont = value;
                    break;
            }
            if (newWidth >= minControlSize && newHeight >= minControlSize)
                view.setLayoutParams(new FrameLayout.LayoutParams(newWidth, newHeight));
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, newFont);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private final class TouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN && mode) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                view.performClick();
                onClick(view);
                return true;
            } else {
                return false;
            }
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    private final class DragDropListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;
                case DragEvent.ACTION_DRAG_LOCATION:
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    View view = (View) event.getLocalState();
                    float x = event.getX();
                    float y = event.getY();
                    view.setX(x-(view.getWidth()/2));
                    view.setY(y-(view.getHeight()/2));
                    view.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                default:
                    break;
            }
            return true;
        }
    }

    public boolean saveBitmapIntoSDCardImage(Bitmap finalBitmap) {
        String fname = "background" + ".jpg";
        File file = new File (getFilesDir(), fname);

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
