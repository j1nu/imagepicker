package com.jinu.imagepickerlib.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListPopupWindow;

import com.jinu.imagepickerlib.PhotoPickerActivity;
import com.jinu.imagepickerlib.R;
import com.jinu.imagepickerlib.adapter.PhotoGridAdapter;
import com.jinu.imagepickerlib.adapter.PhotoSelectedAdapter;
import com.jinu.imagepickerlib.adapter.PopupDirectoryListAdapter;
import com.jinu.imagepickerlib.entity.Photo;
import com.jinu.imagepickerlib.entity.PhotoDirectory;
import com.jinu.imagepickerlib.event.OnPhotoClickListener;
import com.jinu.imagepickerlib.utils.ImageCaptureManager;
import com.jinu.imagepickerlib.utils.MediaStoreHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.app.Activity.RESULT_OK;
import static com.jinu.imagepickerlib.PhotoPickerActivity.EXTRA_SHOW_GIF;
import static com.jinu.imagepickerlib.utils.MediaStoreHelper.INDEX_ALL_PHOTOS;

public class PhotoPickerFragment extends Fragment {

    private Context mContext = null;
    private Activity mActivity = null;

    private ImageCaptureManager captureManager;
    private RecyclerView selectedRecyclerView;
    private PhotoSelectedAdapter selectedPhotoAdapter;
    private PhotoGridAdapter photoGridAdapter;

    private PopupDirectoryListAdapter listAdapter;
    private List<PhotoDirectory> directories;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity().getApplicationContext();
        mActivity = this.getActivity();

        directories = new ArrayList<>();

        captureManager = new ImageCaptureManager(getActivity());

        Bundle mediaStoreArgs = new Bundle();
        if (getActivity() instanceof PhotoPickerActivity) {
            mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, ((PhotoPickerActivity) getActivity()).isShowGif());
        }

        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs,
                new MediaStoreHelper.PhotosResultCallback() {
                    @Override
                    public void onResultCallback(List<PhotoDirectory> dirs) {
                        directories.clear();
                        directories.addAll(dirs);
                        photoGridAdapter.notifyDataSetChanged();
                        listAdapter.notifyDataSetChanged();
                    }
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);

        final View rootView = inflater.inflate(R.layout.util_fragment_photo_picker, container, false);

        selectedPhotoAdapter = new PhotoSelectedAdapter(getActivity(), this);
        photoGridAdapter = new PhotoGridAdapter(getActivity(), directories , ((PhotoPickerActivity)getActivity()).isCheckBoxOnly, selectedPhotoAdapter);
        listAdapter = new PopupDirectoryListAdapter(getActivity(), directories);

        selectedRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_selected_photos);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
        selectedRecyclerView.setLayoutManager(linearLayoutManager);
        selectedPhotoAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                selectedRecyclerView.scrollToPosition(selectedPhotoAdapter.getItemCount() - 1);
            }

//            @Override
//            public void onItemRangeRemoved(int positionStart, int itemCount) {
//                super.onItemRangeRemoved(positionStart, itemCount);
//
//                System.out.println("removed " + positionStart);
//                selectedRecyclerView.scrollToPosition(positionStart);
//            }
        });
        selectedRecyclerView.setAdapter(selectedPhotoAdapter);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.rv_photos);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), ((PhotoPickerActivity)getActivity()).maxGrideItemCount);
//        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(((PhotoPickerActivity)getActivity()).maxGrideItemCount, OrientationHelper.VERTICAL);
//        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(photoGridAdapter);
        recyclerView.setItemAnimator(null);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new ItemDecoration(3, ((PhotoPickerActivity)getActivity()).maxGrideItemCount));

        Toolbar mToolbar = (Toolbar) rootView.findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        ((PhotoPickerActivity) getActivity()).setSupportActionBar(mToolbar);

        ActionBar actionBar = ((PhotoPickerActivity) getActivity()).getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actionBar.setElevation(25);
        }

        final Button btSwitchDirectory = (Button) rootView.findViewById(R.id.btn_toolbar);

        final ListPopupWindow listPopupWindow = new ListPopupWindow(getActivity());
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setAnchorView(btSwitchDirectory);
        listPopupWindow.setAdapter(listAdapter);
        listPopupWindow.setModal(true);
//        listPopupWindow.setDropDownGravity(Gravity.BOTTOM);
//        listPopupWindow.setAnimationStyle(R.style.Animation_AppCompat_DropDownUp);
//        listPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_popup_menu));

        listPopupWindow.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listPopupWindow.dismiss();
                PhotoDirectory directory = directories.get(position);
                btSwitchDirectory.setText(directory.getName());
                photoGridAdapter.setCurrentDirectoryIndex(position);
                photoGridAdapter.notifyDataSetChanged();
            }
        });

        photoGridAdapter.setOnPhotoClickListener(new OnPhotoClickListener() {
            @Override
            public void onClick(View v, int position, boolean showCamera) {
                final int index = showCamera ? position - 1 : position;

                List<String> photos = photoGridAdapter.getCurrentPhotoPaths();

                int[] screenLocation = new int[2];
                v.getLocationOnScreen(screenLocation);
                ImagePagerFragment imagePagerFragment =
                        ImagePagerFragment.newInstance(photos, index, screenLocation,
                                v.getWidth(), v.getHeight());

                ((PhotoPickerActivity) getActivity()).addImagePagerFragment(imagePagerFragment);
            }
        });

        photoGridAdapter.setOnCameraClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = captureManager.dispatchTakePictureIntent();
                    startActivityForResult(intent, ImageCaptureManager.REQUEST_TAKE_PHOTO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        btSwitchDirectory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listPopupWindow.isShowing()) {
                    listPopupWindow.dismiss();
                } else if (!getActivity().isFinishing()) {
                    listPopupWindow.setHeight(Math.round(rootView.getHeight() * 0.8f));
                    listPopupWindow.show();
                }

            }
        });

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ImageCaptureManager.REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            captureManager.galleryAddPic();
            if (directories.size() > 0) {

                String path = captureManager.getCurrentPhotoPath();
                PhotoDirectory directory = directories.get(INDEX_ALL_PHOTOS);
                directory.getPhotos().add(INDEX_ALL_PHOTOS, new Photo(path.hashCode(), path));
                directory.setCoverPath(path);

//                String temp_patch = getLastPhotoPath();
//                temp_patch = temp_patch.replace(".jpg", "");
//                String newFileName = new File(path).getName();
//
//                if (path.contains(temp_patch)) {
//                }
                photoGridAdapter.notifyDataSetChanged();
            }
        }else{
            photoGridAdapter.notifyDataSetChanged();
        }
    }


    public PhotoGridAdapter getPhotoGridAdapter() {
        return photoGridAdapter;
    }

    public class ItemDecoration extends RecyclerView.ItemDecoration {
        private int dp;
        private int spanCount;

        public ItemDecoration(int dp, int spanCount) {
            this.dp = dp;
            this.spanCount = spanCount;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);

            GridLayoutManager.LayoutParams lp = (GridLayoutManager.LayoutParams) view.getLayoutParams();
            int spanIndex = lp.getSpanIndex();

            if (spanIndex == 0) {
                outRect.right = dpToPx(2);
            }
            else if (spanIndex == 1) {
                outRect.left = dpToPx(1);
                outRect.right = dpToPx(1);
            }
            else if (spanIndex == 2) {
                outRect.left = dpToPx(2);
            }

            outRect.bottom = dpToPx(dp);
        }

        private int dpToPx(int dp) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    (float) dp,
                    getResources().getDisplayMetrics()
            );
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        captureManager.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Bundle mediaStoreArgs = new Bundle();
        if (getActivity() instanceof PhotoPickerActivity) {
            mediaStoreArgs.putBoolean(EXTRA_SHOW_GIF, ((PhotoPickerActivity) getActivity()).isShowGif());
        }

        MediaStoreHelper.getPhotoDirs(getActivity(), mediaStoreArgs, new MediaStoreHelper.PhotosResultCallback() {
            @Override
            public void onResultCallback(List<PhotoDirectory> dirs) {
                directories.clear();
                directories.addAll(dirs);
                photoGridAdapter.notifyDataSetChanged();
                listAdapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        captureManager.onRestoreInstanceState(savedInstanceState);
    }

    public ArrayList<String> getSelectedPhotoPaths() {
        return photoGridAdapter.getSelectedPhotoPaths();
    }

    public String getLastPhotoPath() {
        final String[] IMAGE_PROJECTION = {
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.Thumbnails.DATA};

        final Uri uriImages = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String cameraPath = "'";
        String FileName = "";
        try {
            final Cursor cursorImages = mActivity.getContentResolver().query(uriImages, IMAGE_PROJECTION, null, null, null);
            if (cursorImages != null && cursorImages.moveToLast()) {
                cameraPath = cursorImages.getString(0);
                cursorImages.close();

                FileName = new File(cameraPath).getName();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileName;
    }

    public void scrollToPosition(int position) {
        if (position != 0)
            selectedRecyclerView.scrollToPosition(position - 1);
    }
}
