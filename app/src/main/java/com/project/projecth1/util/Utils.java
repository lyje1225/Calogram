package com.project.projecth1.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {

    /* 숫자 체크 */
    public static boolean isNumeric(String str) {
        boolean chk = false;

        try{
            Double.parseDouble(str) ;
            chk = true ;
        } catch (Exception ignored) {}

        return chk;
    }

    /* 날자 체크 */
    public static boolean isDate(String date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());
        simpleDateFormat.setLenient(false);
        try {
            simpleDateFormat.parse(date);
            return true;
        } catch (Exception Ex) {
            return false;
        }
    }

    /* 이메일 체크 */
    public static boolean isEmail(String email) {
        String regEx = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        return Pattern.matches(regEx, email);
    }

    /* 영양소 체크 (이미지에서 텍스트 인식할 때 사용됨) */
    public static boolean isNutrient(String text) {
        String regEx = "^(나트륨|탄수화물|당류|지방|단백질)\\d+m?g$";

        return Pattern.matches(regEx, text);
    }

    /* 숫자 콤마 표시 */
    public static String formatComma(long value) {
        DecimalFormat df = new DecimalFormat("#,###");
        return df.format(value);
    }

    /* 날자 구하기 */
    public static String getDate(String format, long timeMillis) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        Date date = new Date(timeMillis);

        return dateFormat.format(date);
    }

    /* 나이 계산 (만 나이) */
    public static int getAge(int birthYear, int birthMonth, int birthDay) {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        int age = currentYear - birthYear;
        // 생일이 지나지 않았으면 -1
        if (birthMonth * 100 + birthDay > currentMonth * 100 + currentDay) {
            age--;
        }

        return age;
    }

    /* 기초대사량 구하기 */
    public static double getBasicMetabolicRate(String gender, int age, double height, double weight) {
        double value = 0;
        if (gender.equals(Constants.Gender.MALE)) {
            // 남자
            value = 66.47 + (13.75 * weight) + (5 * height) - (6.76 * age);
        } else {
            // 여자
            value = 655.1 + (9.56 * weight) + (1.85 * height) - (4.68 * age);
        }

        return value;
    }

    /* 식사시기 얻기 */
    public static String getMealKind(int kind) {
        String mealKind = "";
        switch (kind) {
            case Constants.MealKind.BREAKFAST:
                mealKind = "아침";
                break;
            case Constants.MealKind.LUNCH:
                mealKind = "점심";
                break;
            case Constants.MealKind.DINNER:
                mealKind = "저녁";
                break;
        }

        return mealKind;
    }

    /* URI 에서 비트맵 얻기 */
    public static Bitmap getBitmapFromUri(Context context, Uri uri, int width, int height) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        // Bitmap 정보를 가져온다.
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, bmOptions);
        if (inputStream != null) {
            inputStream.close();
        }

        int photoWidth = bmOptions.outWidth; // 사진의 가로 사이즈 구하기
        int photoHeight = bmOptions.outHeight; // 사진의 세로 사이즈 구하기

        int scaleFactor = 1;

        // 축소율이 높은 쪽을 기준으로 설정
        int originalSize = photoWidth;
        int resize = width;
        if (((double) photoWidth / width) < ((double) photoHeight / height)) {
            originalSize = photoHeight;
            resize = height;
        }

        while ((originalSize / 2) > resize) {
            originalSize /= 2;
            scaleFactor *= 2;
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        inputStream = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
        if (inputStream != null) {
            inputStream.close();
        }
        return bitmap;
    }

    /* 회전 각도 구하기 */
    public static int getExifOrientation(Context context, Uri uri) {
        try {
            return getExifOrientation(context.getContentResolver().openInputStream(uri));
        } catch (IOException e) {
            return 0;
        }
    }

    /* 회전 각도 구하기 */
    public static int getExifOrientation(InputStream inputStream) {
        try {
            return getExifOrientation(new ExifInterface(inputStream));
        } catch (IOException e) {
            return 0;
        }
    }

    /* 회전 각도 구하기 */
    public static int getExifOrientation(ExifInterface exif) {
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        int degree = 0;
        if (orientation != -1) {
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        }

        return degree;
    }

    /* 이미지 회전하기 */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degree) {
        if (degree != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            //matrix.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            matrix.setRotate(degree);

            try {
                Bitmap tmpBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                if (bitmap != tmpBitmap) {
                    bitmap.recycle();
                    bitmap = tmpBitmap;
                }
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    /* 카메라 앱을 활용하여 사진찍기 (activity) */
    public static Uri goCamera(ActivityResultLauncher<Intent> launcher, Context context,
                               String subFolder, String filePrefix, String fileSuffix) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = filePrefix + timeStamp + fileSuffix;
        Log.d("goCamera", "fileName:" + fileName);

        // 갤러리에 추가
        Uri contentUri = storeToGallery(context, Environment.DIRECTORY_PICTURES, subFolder, fileName);

        if (contentUri != null) {
            Log.d("goCamera", "contentUri:" + contentUri.toString());
        } else {
            return null;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        launcher.launch(intent);

        return contentUri;
    }

    /* 갤러리에 지정한 폴더와 지정한 파일이름으로 저장 */
    public static Uri storeToGallery(Context context, String folder, String subFolder, String fileName) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/*");

        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 29 버전 이후 버전부터 적용
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, folder + subFolder);
            //contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);

            uri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            // 28 버전까지
            File newFolder = new File(Environment.getExternalStoragePublicDirectory(folder) + subFolder);
            if (!newFolder.exists()) {
                if (!newFolder.mkdir()) {
                    // 폴더 생성이 안됨
                    newFolder = Environment.getExternalStoragePublicDirectory(folder);
                }
            }

            String location = newFolder.getAbsolutePath() + File.separator + fileName;
            contentValues.put(MediaStore.Images.Media.DATA, location);

            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        //Log.d("WidgetUtils", "uri" + uri.toString());
        return context.getContentResolver().insert(uri, contentValues);
    }
}
