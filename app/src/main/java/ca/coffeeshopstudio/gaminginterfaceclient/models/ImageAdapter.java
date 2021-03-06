package ca.coffeeshopstudio.gaminginterfaceclient.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import ca.coffeeshopstudio.gaminginterfaceclient.R;

/**
 * Copyright [2019] [Terence Doerksen]
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ImageAdapter extends BaseAdapter {
    public static final int[] builtIn = Constants.buttons;
    private int customButtonCount;
    private Context context;
    private LayoutInflater inflater;

    public ImageAdapter(Context context, int customButtonCount) {
        inflater = LayoutInflater.from(context);
        this.customButtonCount = customButtonCount;
        this.context = context;
    }

    @Override
    public int getCount() {
        return builtIn.length + 1 + customButtonCount;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View view = convertView;
        //build / retrieve a cell in the grid
        if (view == null) {
            view = inflater.inflate(R.layout.item_grid_image, parent, false);
            holder = new ViewHolder();
            assert view != null;

            holder.textView = view.findViewById(R.id.text);
            holder.imageView = view.findViewById(R.id.image);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }


        if (position == 0) {
            holder.textView.setVisibility(View.VISIBLE);
            holder.imageView.setVisibility(View.GONE);
            //show "add" item
        } else if (position <= customButtonCount) {
            holder.textView.setVisibility(View.GONE);
            String path = context.getFilesDir() + "/button_" + (position - 1) + ".png";
            Picasso.get().setLoggingEnabled(true);
            Picasso.get()
                    .load(new File(path))
                    .error(R.mipmap.ic_launcher)
                    .fit().centerInside()
                    .into(holder.imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.imageView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onError(Exception e) {
                            holder.imageView.setVisibility(View.INVISIBLE);
                        }
                    });
        } else if (position - customButtonCount <= builtIn.length) {
            holder.textView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            holder.imageView.setImageResource(builtIn[position - customButtonCount - 1]);
            holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        return view;
    }
}

class ViewHolder {
    ImageView imageView;
    TextView textView;
}


class Constants {
    static final int[] buttons = new int[]{
            R.drawable.button_neon,
            R.drawable.button_neon_pushed,
            R.drawable.button_blue,
            R.drawable.button_blue_dark,
            R.drawable.button_green,
            R.drawable.button_green_dark,
            R.drawable.button_green_alt,
            R.drawable.button_green_alt_dark,
            R.drawable.button_purple,
            R.drawable.button_purple_dark,
            R.drawable.button_red,
            R.drawable.button_red_dark,
            R.drawable.button_yellow,
            R.drawable.button_yellow_dark
    };


}