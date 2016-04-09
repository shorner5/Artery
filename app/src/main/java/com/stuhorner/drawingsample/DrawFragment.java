package com.stuhorner.drawingsample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DrawFragment extends Fragment {

    private Toolbar toolbar_bottom;
    private CustomView customView;
    private RecyclerView scrollPalette;
    private List<Integer> colors = new ArrayList<>();
    private List<Integer> radii = new ArrayList<>();
    LinearLayoutManager layoutManager;
    boolean paletteMode = true, ongoingAnimation = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_draw, container, false);
        customView = (CustomView) view.findViewById(R.id.custom_view);
        setHasOptionsMenu(true);
        setBackgroundImage();
        hasTopToolbar();

        scrollPalette = (RecyclerView) view.findViewById(R.id.palette);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        initPalette();

        scrollPalette.setLayoutManager(layoutManager);
        PaletteAdapter adapter = new PaletteAdapter(colors, radii, getContext());
        scrollPalette.setAdapter(adapter);

        toolbar_bottom = (Toolbar) view.findViewById(R.id.toolbar_bottom);
        toolbar_bottom.inflateMenu(R.menu.menu_drawing);
        toolbar_bottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                handleDrawingIconTouched(item.getItemId());
                return false;
            }
        });
        handlePaletteClicks();
        return view;
    }

    private void initPalette() {
        colors.clear();
        radii.clear();
        colors.add(getResources().getColor(R.color.green));
        colors.add(getResources().getColor(R.color.deepBlue));
        colors.add(getResources().getColor(R.color.blue));
        colors.add(getResources().getColor(R.color.orange));
        colors.add(getResources().getColor(R.color.red));
        colors.add(getResources().getColor(R.color.pink));
        colors.add(getResources().getColor(R.color.purple));
        colors.add(getResources().getColor(R.color.brown));
        colors.add(Color.BLACK);
        colors.add(Color.WHITE);

        for (int i = 0; i < colors.size(); i++) {
            radii.add(84);
        }
    }
    private void initStrokes() {
        radii.clear();
        colors.clear();
        radii.add(5);
        radii.add(getResources().getInteger(R.integer.small_size));
        radii.add(getResources().getInteger(R.integer.medium_size));
        radii.add(getResources().getInteger(R.integer.large_size));
        radii.add(getResources().getInteger(R.integer.max_size));

        for (int i = 0; i < radii.size(); i++) {
            colors.add(customView.getPaintColor());
        }
    }

    private void handlePaletteClicks(){
        ItemClickSupport.addTo(scrollPalette).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                if (paletteMode) {
                    customView.setPaintColor(colors.get(position));
                    v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.translate_down_up));
                } else {
                    List<Float> animRadiiList = initAnimList();
                    customView.setBrushSize(radii.get(position));
                    Log.d("Size:", Float.toString(animRadiiList.get(position)));

                    AnimationSet animationSet = new AnimationSet(true);
                    animationSet.setInterpolator(new DecelerateInterpolator());
                    animationSet.setFillAfter(true);

                    ScaleAnimation scaleAnimUp = new ScaleAnimation(animRadiiList.get(position) / .8f, .8f, animRadiiList.get(position) / .8f, .8f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                    scaleAnimUp.setDuration(200);

                    ScaleAnimation scaleAnimDown = new ScaleAnimation(.8f, animRadiiList.get(position) / .8f, .8f, animRadiiList.get(position) / .8f, Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
                    scaleAnimDown.setDuration(200);
                    scaleAnimDown.setStartOffset(500);

                    animationSet.addAnimation(scaleAnimDown);
                    animationSet.addAnimation(scaleAnimUp);
                    v.startAnimation(animationSet);
                }
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            saveImage(false);
            Fragment fragment = new GalleryFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
            getActivity().getIntent().putExtra("page", MainActivity.HIDE_MENU);
            getActivity().invalidateOptionsMenu();
            if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.action_gallery));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleDrawingIconTouched(int itemId) {
        switch (itemId){
            case R.id.action_delete:
                deleteDialog();
                break;
            case R.id.action_undo:
                customView.onClickUndo();
                break;
            case R.id.action_redo:
                customView.onClickRedo();
                break;
            case R.id.action_save:
                saveImage(true);
                break;
            case R.id.action_brush:
                if (paletteMode && !ongoingAnimation)
                    changeStroke();
                else if (!ongoingAnimation)
                    changePalette();
                break;
            default:
                break;
        }
    }

    private void deleteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
        deleteDialog.setTitle(getString(R.string.delete_drawing));
        deleteDialog.setMessage(getString(R.string.delete_drawing_small));
        deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                customView.eraseAll();
                dialogInterface.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        deleteDialog.show();
    }

    private void setBackgroundImage() {
        String backgroundImage = getActivity().getIntent().getStringExtra("edit_image");
        getActivity().getIntent().removeExtra("edit_image");
        Bitmap bitmap = BitmapFactory.decodeFile(backgroundImage);
        if (backgroundImage != null) {
            customView.setBackground(new BitmapDrawable(getResources(), bitmap));
        } else {
            customView.setBackgroundColor(getResources().getColor(android.R.color.white));
        }
    }

    private void saveImage(boolean toGallery) {
        customView.setDrawingCacheEnabled(true);
        customView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_AUTO);
        Bitmap bm = customView.getDrawingCache();

        String path; File dir;
        if (toGallery) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
            dir = new File(path + "/" + getString(R.string.app_name));
        }
        else {
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            dir = cw.getExternalFilesDir(null);
            Log.d("Directory", "" + dir.getAbsolutePath());
            SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(getString(R.string.directory), dir.getAbsolutePath());
            editor.apply();
        }

        String title = "drawing" + System.currentTimeMillis() + ".png";
        String saved = MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), customView.getDrawingCache(), title, "drawing");

        try {
            if (!dir.isDirectory() || !dir.exists()){
                dir.mkdirs();
            }
            File file = new File(dir, title);
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "Not enough space on this device!", Toast.LENGTH_SHORT).show();
        } catch(IOException e){
            e.printStackTrace();
            Toast.makeText(getContext(), "Not enough space on this device!", Toast.LENGTH_SHORT).show();
        }

        if (saved != null && toGallery) {
            Snackbar.make(customView, R.string.saved, Snackbar.LENGTH_SHORT).show();
        }
        customView.destroyDrawingCache();
    }

    private void changeStroke() {
        toolbar_bottom.getMenu().findItem(R.id.action_brush).setIcon(R.drawable.ic_color);

        paletteMode = false;

        //isolate 5 circles, remove others
        for (int i = colors.size() - 1; i >= 0; i--) {
            if (i < layoutManager.findFirstVisibleItemPosition() || i > layoutManager.findFirstVisibleItemPosition() + 4) {
                colors.remove(i);
                radii.remove(i);
                scrollPalette.getAdapter().notifyItemRemoved(i);
            }
        }

        //initialize radii array
        initStrokes();

        //initialize scale animations
        List<Float> animRadiiList = initAnimList();
        for (int i = 0; i < radii.size(); i++) {
            final int j = i;
            final RecyclerView.ViewHolder view = scrollPalette.findViewHolderForAdapterPosition(i);

            final ScaleAnimation scaleAnim= new ScaleAnimation(1, animRadiiList.get(i), 1, animRadiiList.get(i), Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
            scaleAnim.setDuration(500);
            scaleAnim.setStartOffset(50 * i);
            scaleAnim.setInterpolator(new BounceInterpolator());
            scaleAnim.setFillAfter(true);

            view.itemView.startAnimation(scaleAnim);
            scaleAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ongoingAnimation = true;
                    ((ImageView)view.itemView).setColorFilter(customView.getPaintColor());
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    ongoingAnimation = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    private List<Float> initAnimList() {
        List<Float> animRadiiList = new ArrayList<>(radii.size());
        animRadiiList.add(.059f);
        animRadiiList.add(.119f);
        animRadiiList.add(.238f);
        animRadiiList.add(.357f);
        animRadiiList.add(.595f);
        return animRadiiList;
    }

    private void changePalette(){
        toolbar_bottom.getMenu().findItem(R.id.action_brush).setIcon(R.drawable.ic_brush);
        paletteMode = true;
        initPalette();
        List<Float>  animRadiiList = initAnimList();

        for (int i = 0; i < 5; i++) {
            final RecyclerView.ViewHolder view = scrollPalette.findViewHolderForAdapterPosition(i);
            final int j = i;

            ScaleAnimation scaleAnim= new ScaleAnimation(animRadiiList.get(i), 1, animRadiiList.get(i), 1, Animation.RELATIVE_TO_SELF, .5f,Animation.RELATIVE_TO_SELF, .5f);
            scaleAnim.setDuration(500);
            scaleAnim.setStartOffset(50 * i);
            scaleAnim.setInterpolator(new BounceInterpolator());
            scaleAnim.setFillAfter(true);

            view.itemView.startAnimation(scaleAnim);
            scaleAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    ongoingAnimation = true;
                    ((ImageView)view.itemView).setColorFilter(colors.get(j));
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //scrollPalette.getAdapter().notifyItemChanged(j);
                    ongoingAnimation = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
        }
    }

    private void hasTopToolbar() {
        SharedPreferences pref = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (pref.getBoolean("isFirstLaunch", false)) {
            //TODO: show a toolbar
        }

        SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        editor.putBoolean("isFirstLaunch", false);
        editor.apply();
    }
}