package com.stuhorner.drawingsample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements GalleryOptionsDialog.OnDialogSelectionListener {
    ArrayList<String> drawings = new ArrayList<>();
    ImageAdapter img;
    int selected = 0;
    GridView gridView; ProgressBar progressBar;
    public static final int REQUEST_CODE = 2;

    @Override
    public void onDialogSelection(int position) {
        switch (position) {
            case GalleryOptionsDialog.SET_AS_CARD:
                //set as the users card
                MyUser.getInstance().setCard(drawings.get(selected));
                Snackbar.make(getView(), R.string.card_set, Snackbar.LENGTH_SHORT).show();
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
                if (gridView.getAdapter().getCount() < 2) {
                    noDeleteDialog();
                }
                else {
                        Log.d("DELETED", "deleted");
                        delete();
                }
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.gallery_layout, container, false);
        final Fragment thisFragment = this;

        progressBar = (ProgressBar) view.findViewById(R.id.gallery_loading);
        gridView = (GridView) view.findViewById(R.id.gallery_grid);
        initData(gridView, progressBar);
        gridView.setColumnWidth((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getInteger(R.integer.gallery_size), Resources.getSystem().getDisplayMetrics()));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), DisplayDrawingActivity.class);
                intent.putExtra("edit_image", drawings.get(i));
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),view,"image_scale");
                startActivityForResult(intent, REQUEST_CODE, options.toBundle());
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

    @Override
    public void onActivityResult (int requestCode, int resultCode, Intent data) {
        Log.d("requestCode, resultCode", requestCode + ", " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            String path = data.getStringExtra("edit_image");
            selected = drawings.indexOf(path);
            onDialogSelection(resultCode);
        }
    }

    private void initData(GridView grid, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);
        GalleryInitTask task = new GalleryInitTask(getActivity(), grid, drawings, img, progressBar);
        task.execute();
    }

    private void noDeleteDialog() {
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
            deleteDialog.setTitle(getString(R.string.no_delete));
            deleteDialog.setMessage(getString(R.string.no_delete_body));
            deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            deleteDialog.show();
        }

    private void delete() {
        //get image as base64 from URI
        Log.d("path", drawings.get(selected));
        Bitmap BitmapToDelete = BitmapFactory.decodeFile(drawings.get(selected));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapToDelete.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        String toDelete = Base64.encodeToString(bytes, Base64.DEFAULT);
        //Find the image on the server
        List<String> drawingsOnServer = MyUser.getInstance().getGallery();
        for (int i = 0; i < drawingsOnServer.size(); i++) {
            if (toDelete.equals(drawingsOnServer.get(i))) {
                MyUser.getInstance().removeFromGallery(i);
            }
        }
        new File(gridView.getAdapter().getItem(selected).toString()).delete();
        drawings.remove(selected);
        ((ImageAdapter)gridView.getAdapter()).reloadImages(drawings);

    }
}

