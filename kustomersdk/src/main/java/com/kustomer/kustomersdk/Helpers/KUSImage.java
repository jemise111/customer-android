package com.kustomer.kustomersdk.Helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;

import com.kustomer.kustomersdk.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Junaid on 1/20/2018.
 */

public class KUSImage {
    //region Properties
    private static List<Integer> defaultNameColors = null;
    //endreigon

    //region Resource Method
    public static Bitmap bitmapFromId(Context mContext, int id){
        return BitmapFactory.decodeResource(mContext.getResources(), id);
    }
    //endregion

    //region Public Methods
    private static Bitmap circularImage(KSize size, int color, int strokeColor, int strokeWidth){
        Bitmap dstBitmap = Bitmap.createBitmap(
                size.getWidth(), // Width
                size.getHeight(), // Height
                Bitmap.Config.ARGB_8888 // Config
        );

        // Initialize a new Canvas to draw circular bitmap
        Canvas canvas = new Canvas(dstBitmap);

        // Initialize a new Paint instance
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        // Calculate the available radius of canvas
        int radius = Math.min(canvas.getWidth(),canvas.getHeight()/2);

        if(strokeWidth > 0) {
            paint.setColor(strokeColor);

            // Set a pixels value to padding around the circle
            canvas.drawCircle(
                    canvas.getWidth() / 2, // cx
                    canvas.getHeight() / 2, // cy
                    radius, // Radius
                    paint // Paint
            );
        }

        paint.setColor(color);
        canvas.drawCircle(
                canvas.getWidth() / 2, // cx
                canvas.getHeight() / 2, // cy
                radius - strokeWidth, // Radius
                paint // Paint
        );

        return dstBitmap;

    }

    private static Bitmap getBitmapWithText(Context mContext, KSize size, int color, int strokeColor, int strokeWidth, String text, int textSize){



        Bitmap src = circularImage(size,color, strokeColor, strokeWidth);

        Resources resources = mContext.getResources();
        float scale = resources.getDisplayMetrics().density;

        Canvas canvas = new Canvas(src);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE); // Text Color
        paint.setTextSize((int)textSize*scale);// Text Size
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)); // Text Overlapping Pattern

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        int x = (canvas.getWidth() / 2);
        int y = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, x, y, paint);


        return src;
    }

    public static Bitmap defaultAvatarBitmapForName(Context context, KSize size, String name, int strokeWidth, int fontSize){
        Bitmap bitmap = new KUSCache().getBitmapFromMemCache(name + "w:" + strokeWidth);
        if(bitmap != null)
            return bitmap;

        List<String> initials = initialsForName(name);

        int letterSum = 0;
        StringBuilder text = new StringBuilder();
        for(String initial : initials){
            letterSum += initial.charAt(0);
            text.append(initial);
        }

        int colorIndex = letterSum % getDefaultNameColors().size();
        bitmap = getBitmapWithText(
                context,
                size,
                ContextCompat.getColor(context,getDefaultNameColors().get(colorIndex)),
                ContextCompat.getColor(context,R.color.kusToolbarBackgroundColor),
                strokeWidth,
                text.toString(),
                fontSize);

        if(bitmap != null)
            new KUSCache().addBitmapToMemoryCache(name + "w:" + strokeWidth,bitmap);

        return bitmap;
    }
    //endregion

    //region Private Methods
    private static List<String> initialsForName(String name){
        int maximumInitialsCount = 3;

        String[] words = name.trim().split(" ");
        List<String> initials = new ArrayList<>();

        for(String word : words){

            if(word.length() > 0) {
                String firstLetter = String.valueOf(word.toUpperCase().charAt(0));
                initials.add(firstLetter);
            }
            if(initials.size() >= maximumInitialsCount)
                break;

        }

        if(initials.size()>0)
            return initials;

        initials.add("*");

        return initials;
    }

    private static List<Integer> getDefaultNameColors(){
        if(defaultNameColors == null) {
            defaultNameColors = new ArrayList<>();
            defaultNameColors.add(R.color.kusDefaultNameColor1);
            defaultNameColors.add(R.color.kusDefaultNameColor2);
            defaultNameColors.add(R.color.kusDefaultNameColor3);
            defaultNameColors.add(R.color.kusDefaultNameColor4);
            defaultNameColors.add(R.color.kusDefaultNameColor5);
            defaultNameColors.add(R.color.kusDefaultNameColor6);
        }

        return defaultNameColors;
    }

    static byte[] getByteArrayFromBitmap(Bitmap bitmap){
        if(bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            return stream.toByteArray();
        }

        return null;
    }

    public static Bitmap getScaledImage(Bitmap bitmap, int maxPixelCount) throws OutOfMemoryError {

        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();

        float imagePixelCount =  srcWidth * srcHeight;
        float scaleDown = (float) Math.min(Math.sqrt(maxPixelCount/imagePixelCount),1.0);

        if (scaleDown < 1) {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleDown, scaleDown);

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } else {
            return bitmap;
        }
    }

    public static Bitmap rotateBitmapIfNeeded(Bitmap bitmap, InputStream inputStream) throws IOException {
        ExifInterface ei = new ExifInterface(inputStream);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED);

        Bitmap rotatedBitmap = null;
        switch(orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(bitmap, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(bitmap, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(bitmap, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = bitmap;
        }

        return rotatedBitmap;
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }
    //endregion

}
