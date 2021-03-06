package ca.coffeeshopstudio.gaminginterfaceclient.views;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ca.coffeeshopstudio.gaminginterfaceclient.R;

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
public class EditBackgroundFragment extends DialogFragment implements View.OnClickListener {
    private int primary;
    private Button btnPrimary;
    private int screenId;

    public static EditBackgroundFragment newInstance(String title, int primary, int screenId) {
        EditBackgroundFragment frag = new EditBackgroundFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putInt("primary", primary);
        args.putInt("screen", screenId);
        frag.setArguments(args);
        return frag;
    }

    // Empty constructor is required for DialogFragment
    // Make sure not to add arguments to the constructor
    // Use `newInstance` instead as shown below
    public EditBackgroundFragment() {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        String title;
        if (getArguments() != null) {
            title = getArguments().getString("title", "Enter Name");
        } else
            title = getString(R.string.default_control_text);
        primary = getArguments().getInt("primary", Color.BLACK);
        screenId = getArguments().getInt("screen", 0);
        getDialog().setTitle(title);
        setupControls(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_background, container);
    }

    private void setupControls(View view) {
        btnPrimary = view.findViewById(R.id.btnColor1);
        btnPrimary.setOnClickListener(this);
        btnPrimary.setTextColor(primary);

        view.findViewById(R.id.btnBackgroundImage).setOnClickListener(this);
        view.findViewById(R.id.btnSave).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        EditDialogListener listener = (EditDialogListener) getActivity();
        switch (view.getId()) {
            case R.id.btnSave:
                listener.onFinishEditBackgroundDialog(btnPrimary.getTextColors().getDefaultColor(), null);
                dismiss();
                break;
            case R.id.btnColor1:
                displayColorPicker(view);
                break;
            case R.id.btnBackgroundImage:
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, EditActivity.OPEN_REQUEST_CODE_BACKGROUND);
                break;
            default:
                break;
        }
    }

    private void displayColorPicker(final View view) {
        ColorPickerDialogBuilder
                .with(getContext())
                .setTitle(getString(R.string.color_picker_title))
                .initialColor(((Button) view).getCurrentTextColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
//                .setOnColorSelectedListener(new OnColorSelectedListener() {
//                    @Override
//                    public void onColorSelected(int selectedColor) {
//                        //toast("onColorSelected: 0x" + Integer.toHexString(selectedColor));
//                    }
//                })
                .setPositiveButton(getString(android.R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        ((Button) view).setTextColor(selectedColor);
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (resultCode == Activity.RESULT_OK)
        {
            if (requestCode == EditActivity.OPEN_REQUEST_CODE_BACKGROUND) {
                if (resultData != null) {
                    Uri currentUri = resultData.getData();
                    File file = null;
                    if (currentUri != null) {
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentUri);
                            file = new File(getContext().getFilesDir(), screenId + "_background.png");
                            FileOutputStream out = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                            out.flush();
                            out.close();
                        } catch (IOException e) {
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        }
                    }

                    EditDialogListener listener = (EditDialogListener) getActivity();
                    listener.onFinishEditBackgroundDialog(-1, file.getAbsolutePath());
                    dismiss();
                }
            }
        }
    }

    public interface EditDialogListener {
        void onFinishEditBackgroundDialog(int primaryColor, String backgroundPath);
    }
}