package by.kirilldrob.bynexchangerates.data;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Html;

/**
 * Created by Администратор on 19.09.2016.
 */

public class Currency {
    public static final Html.ImageGetter IMAGE_GETTER = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            return new ColorDrawable(Color.TRANSPARENT);
        }
    };
    public final String  charCode, scale, name, rate;

    @SuppressWarnings("deprecation")
    public Currency(String charCode,  String rate, String name,String scale) {
        this.charCode = charCode;
        this.name = name;
        this.rate = rate;
        this.scale = scale;

    }




}
