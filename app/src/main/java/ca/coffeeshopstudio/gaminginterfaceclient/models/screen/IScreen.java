package ca.coffeeshopstudio.gaminginterfaceclient.models.screen;

import android.graphics.drawable.Drawable;

import java.util.List;

import ca.coffeeshopstudio.gaminginterfaceclient.models.GICControl;

/**
 * TODO: HEADER COMMENT HERE.
 */
public interface IScreen {
    String getName();

    void setName(String newName);

    int getScreenId();

    String getBackgroundFile();

    Drawable getImage(String filename);

    void addControl(GICControl control);

    int getNewControlId();

    List<GICControl> getControls();

    void setScreenId(int newId);

    void removeControl(GICControl control);

    Drawable getBackground();

    void setBackground(Drawable background);

    void setBackgroundColor(int backgroundColor);

    int getBackgroundColor();

    void setBackgroundFile(String backgroundPath);

}
