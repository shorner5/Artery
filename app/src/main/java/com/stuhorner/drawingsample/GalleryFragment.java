package com.stuhorner.drawingsample;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class GalleryFragment extends Fragment implements GalleryOptionsDialog.OnDialogSelectionListener {
    ArrayList<String> drawings = new ArrayList<>();
    ImageAdapter img;
    int selected = 0;
    public static final int RESULT_CODE = 2;
    public static final int RESULT_CANCEL = 3;

    @Override
    public void onDialogSelection(int position) {
        switch (position) {
            case GalleryOptionsDialog.SET_AS_CARD:
                //set as the users card
                break;
            case GalleryOptionsDialog.EDIT:
                getActivity().getIntent().putExtra("edit_image", drawings.get(selected));
                DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
                drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                Fragment fragment = new DrawFragment();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                getActivity().getIntent().putExtra("page", MainActivity.ON_DRAWING);
                getActivity().invalidateOptionsMenu();
                break;
            case GalleryOptionsDialog.REMOVE:
                boolean delete = new File(img.getItem(selected).toString()).delete();
                if (delete) {
                    initData();
                    img.reloadImages(drawings);
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_layout, container, false);
        initData();
        final Fragment thisFragment = this;

        if (drawings.size() != 0) { (view.findViewById(R.id.gallery_empty)).setVisibility(View.INVISIBLE); }
        Log.d("bitmap", "" + drawings.size());

        GridView gridView = (GridView) view.findViewById(R.id.gallery_grid);
        img = new ImageAdapter(getContext(), drawings);
        gridView.setAdapter(img);
        gridView.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getInteger(R.integer.gallery_size), Resources.getSystem().getDisplayMetrics()));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), DisplayDrawingActivity.class);
                intent.putExtra("edit_image", drawings.get(i));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),view,"image_scale");
                startActivityForResult(thisFragment, intent, RESULT_CODE, options.toBundle());
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                GalleryOptionsDialog options = new GalleryOptionsDialog();
                options.setTargetFragment(thisFragment, 0);
                options.show(getActivity().getSupportFragmentManager(), "dialog");
                selected = i;
                return true;
            }
        });

        return view;
    }

    public void startActivityForResult(Fragment fragment, Intent intent,
                                              int requestCode, Bundle options) {
        if (Build.VERSION.SDK_INT >= 16) {
            if ((requestCode & 0xffff0000) != 0) {
                throw new IllegalArgumentException("Can only use lower 16 bits" +
                        " for requestCode");
            }
            if (requestCode != -1) {
                try {
                    Field mIndex = Fragment.class.getDeclaredField("mIndex");
                    mIndex.setAccessible(true);
                    requestCode = ((mIndex.getInt(this) + 1) << 16) + (requestCode & 0xffff);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException(e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            ActivityCompat.startActivityForResult(fragment.getActivity(), intent,
                    requestCode, options);
        } else {
            fragment.getActivity().startActivityFromFragment(this, intent, requestCode);
        }
    }

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            Log.d("result:", Integer.toString(resultCode));
            String path = data.getStringExtra("edit_image");
            Log.d("path", path);
            selected = drawings.indexOf(path);
            Log.d("selected:", Integer.toString(selected));
            onDialogSelection(resultCode);
        }
    }

    private void initData() {
        drawings.clear();
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        String dir = sharedPreferences.getString(getString(R.string.directory), null);
        Log.d("Directory:", dir);
        if (dir != null) {
            File directory = new File(dir);
            File[] files = directory.listFiles();
            for (File file : files) {
                try {
                    if (!file.exists()) continue;
                    drawings.add(file.getAbsolutePath());
                } catch (Exception e) { return; }
            }
        }
    }
}

