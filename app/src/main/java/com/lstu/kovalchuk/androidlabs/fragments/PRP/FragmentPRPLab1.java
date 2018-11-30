package com.lstu.kovalchuk.androidlabs.fragments.PRP;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lstu.kovalchuk.androidlabs.R;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FragmentPRPLab1 extends Fragment {

    private static int SELECT_PICTURE = 1;
    private static final String TAG = "FragmentPRPLab1";
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 1;
    private boolean mExtStrgPermissionGranted = false;
    private static final String EXT_STRG = Manifest.permission.READ_EXTERNAL_STORAGE;
    private ProgressBar progressBar;
    private ProgressBar progressBarHor;
    private ImageView imageView1;
    private ImageView imageView2;
    private TextView tvCountProc;
    private TextView tvRadius;
    private static int COUNT_PROC = 1;
    private static int RADIUS = 1;
    private CombineImg combineImg;

    public FragmentPRPLab1() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_prplab1, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        progressBarHor = getView().findViewById(R.id.prp_lab1_progressBarHor);
        progressBar = getView().findViewById(R.id.prp_lab1_progressBar);
        imageView1 = getView().findViewById(R.id.prp_lab1_ImgView1);
        imageView2 = getView().findViewById(R.id.prp_lab1_ImgView2);
        tvCountProc = getView().findViewById(R.id.prp_lab1_CountProc);
        tvRadius = getView().findViewById(R.id.prp_lab1_Radius);
        FloatingActionButton fabAddImg = getView().findViewById(R.id.prp_lab1_fabAdd);
        fabAddImg.setOnClickListener(v -> {
            // Выбор изображения из памяти
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
        });
        SeekBar sbCountProc = getView().findViewById(R.id.prp_lab1_SeekBarProc);
        sbCountProc.setMax(Runtime.getRuntime().availableProcessors() - 1);
        sbCountProc.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int count_proc = 1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                count_proc = seekBar.getProgress() + 1;
                tvCountProc.setText(MessageFormat.format("Количество ядер: {0}", seekBar.getProgress() + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                COUNT_PROC = count_proc;
            }
        });
        SeekBar sbRadius = getView().findViewById(R.id.prp_lab1_SeekBarRadius);
        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int radius = 1;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = seekBar.getProgress() + 1;
                tvRadius.setText(MessageFormat.format("Радиус размытия: {0}", seekBar.getProgress() + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RADIUS = radius;
            }
        });

        getExtStoragePermission();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    // Метод обработки результата попытки получения изображения из памяти
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri imageUri = data.getData();

                // Проверяем есть ли права доступа к sdcard
                if (!mExtStrgPermissionGranted) {
                    Toast.makeText(getContext(), "Нет прав для доступа к памяти", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Если есть, то получаем bitmap изображения
                InputStream inputStream;
                Bitmap bitmap;
                try {
                    assert imageUri != null;
                    inputStream = getActivity().getApplicationContext().getContentResolver().openInputStream(imageUri);
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    imageView1.setImageBitmap(bitmap);
                } catch (Exception ex) {
                    Log.e(TAG, "onActivityResult: " + ex.getMessage());
                    return;
                }

                // Вместо значка отсутствующего изображения,
                // отображаем progressBar
                progressBar.setVisibility(View.VISIBLE);
                imageView2.setVisibility(View.GONE);

                // Режем изображение на bitmaps по вертикали
                combineImg = new CombineImg(bitmap);
                int widthOneFragment = bitmap.getWidth() / COUNT_PROC;
                List<Bitmap> listBitmap = new ArrayList<>();
                List<AsyncTask> listAT = new ArrayList<>();
                for (int i = 0; i < COUNT_PROC; i++) {
                    if (COUNT_PROC > 1) {
                        listBitmap.add(Bitmap.createBitmap(bitmap, i * widthOneFragment, 0, widthOneFragment, bitmap.getHeight()));
                        // Вызываем в каждом потоке функцию размытия
                        listAT.add(new AsyncBlurImg(listBitmap.get(i), i).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));
                    } else {
                        AsyncTask at = new AsyncBlurImg(bitmap).execute();
                    }
                }
            }
        }
    }

    // Класс обработки одной части изображения
    @SuppressLint("StaticFieldLeak")
    public class AsyncBlurImg extends AsyncTask<Void, Integer, Bitmap> {
        private Bitmap bmp;
        private int number;

        AsyncBlurImg(Bitmap bmp, int number) {
            this.bmp = bmp;
            this.number = number;
        }

        AsyncBlurImg(Bitmap bmp) {
            this.bmp = bmp.copy(Bitmap.Config.ARGB_8888, true);
            number = 0;
        }

        // Перед вычислениями готовим ProgressBar Horizontal
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (number == 0) {
                progressBarHor.setMax(bmp.getWidth());
                progressBarHor.setProgress(0);
            }
        }

        // Выполнение вычислений
        @Override
        protected Bitmap doInBackground(Void... voids) {
            for (int i = 0; i < bmp.getWidth(); i++) {
                for (int k = 0; k < bmp.getHeight(); k++) {
                    bmp.setPixel(i, k, getNewColor(i, k, RADIUS, bmp));
                }
                if (number == 0) publishProgress(i + 1);
            }

            return bmp;
        }

        // Обновление ProgressBar Horizontal
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBarHor.setProgress(values[0]);
        }

        // После выполнения расчетов
        @Override
        protected void onPostExecute(Bitmap bmp) {
            // Передаем очередной фрагмент изображения для соединения
            combineImg.combineImg(bmp, number);
        }
    }

    // Класс для обратного соединения частей изображения и
    // отображения их на экране
    public class CombineImg {
        private Bitmap bmp;
        private Canvas canvas;
        private Paint paint;
        private int counter;

        CombineImg(Bitmap bmp) {
            this.bmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas();
            canvas.setBitmap(this.bmp);
            paint = new Paint();
            counter = 0;
        }

        private void combineImg(Bitmap fragmenImg, int number) {
            canvas.drawBitmap(fragmenImg, bmp.getWidth() / COUNT_PROC * number, 0, paint);
            counter++;
            if (counter == COUNT_PROC) {
                imageView2.setImageBitmap(bmp);
                progressBar.setVisibility(View.GONE);
                imageView2.setVisibility(View.VISIBLE);
            }
        }
    }

    // Получение прав доступа к данным местоположения
    private void getExtStoragePermission() {
        Log.d(TAG, "getLocationPermission: получение прав доступа к данным местоположения");
        String[] permission = {EXT_STRG};

        // Проверка предоставления прав доступа к данным местоположения
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                EXT_STRG) == PackageManager.PERMISSION_GRANTED) {
            mExtStrgPermissionGranted = true;
        } else {
            // Если права не были предоставлены
            // Вызов диалогового окна для предоставления прав доступа к данным местоположения
            ActivityCompat.requestPermissions(getActivity(),
                    permission,
                    MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE);
        }
    }

    // Функция обработки результата получения прав доступа к накопителю
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            // Если хоть какое-то права отсутствует
                            mExtStrgPermissionGranted = false;
                            return;
                        }
                    }
                    // Если все права были предоставлены
                    mExtStrgPermissionGranted = true;
                }
            }
        }
    }

    // Функция получения среднего значения
    // ближайших пикселей.
    public static int getNewColor(int x, int y, int n, Bitmap bmp) {
        int pixel;

        int alphaValue = 0;
        int redValue = 0;
        int blueValue = 0;
        int greenValue = 0;
        double counter = 0;

        for (int i = x - n; i < x + n + 1; i++) {
            if (i < 0 || i >= bmp.getWidth()) {
                continue;
            }
            for (int k = y - n; k < y + n + 1; k++) {
                if (k < 0 || k >= bmp.getHeight()) {
                    continue;
                }
                counter++;
                pixel = bmp.getPixel(i, k);
                alphaValue += Color.alpha(pixel);
                redValue += Color.red(pixel);
                blueValue += Color.blue(pixel);
                greenValue += Color.green(pixel);
            }
        }

        alphaValue /= counter;
        redValue /= counter;
        blueValue /= counter;
        greenValue /= counter;

        return Color.argb(alphaValue, redValue, greenValue, blueValue);
    }
}
