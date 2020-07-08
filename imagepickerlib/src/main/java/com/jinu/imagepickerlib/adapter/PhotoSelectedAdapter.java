package com.jinu.imagepickerlib.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.jinu.imagepickerlib.R;
import com.jinu.imagepickerlib.entity.Photo;
import com.jinu.imagepickerlib.entity.PhotoDirectory;
import com.jinu.imagepickerlib.fragment.PhotoPickerFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

public class PhotoSelectedAdapter extends RecyclerView.Adapter<PhotoSelectedAdapter.SelectedPhotoViewHolder> {

    private LayoutInflater inflater;
    private Context mContext;
    private PhotoPickerFragment photoPickerFragment;

    private PhotoGridAdapter photoGridAdapter;

    public PhotoSelectedAdapter(Context mContext, PhotoPickerFragment photoPickerFragment) {
        this.mContext = mContext;
        this.photoPickerFragment = photoPickerFragment;
        inflater = LayoutInflater.from(mContext);
    }

    public void setPhotoAdapter(PhotoGridAdapter photoGridAdapter) {
        this.photoGridAdapter = photoGridAdapter;
    }

    @NonNull
    @Override
    public SelectedPhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.util_item_selected_photo, parent, false);
        SelectedPhotoViewHolder holder = new SelectedPhotoViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedPhotoViewHolder holder, final int position) {
        List<Photo> photos = photoGridAdapter.getSelectedPhotos();
        final Photo photo = photos.get(position);

        Uri uri = FileProvider.getUriForFile(mContext, "com.jinu.imagepickerlib.fileprovider", new File(photo.getPath()));
        Glide.with(mContext)
                .load(uri)
                .apply(new RequestOptions()
                        .placeholder(R.color.img_loding_placeholder)
                        .error(R.color.image_loading_error_color)
                        .centerCrop())
                .into(holder.ivPhoto);

        holder.ivCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                boolean isEnable = true;

                final int selectedPhotoPosition = photoGridAdapter.selectedPhotosPosition.get(position);

                if (photoGridAdapter.getOnItemCheckListener() != null) {
                    isEnable = photoGridAdapter.getOnItemCheckListener().OnItemCheck(selectedPhotoPosition, photo, true, photoGridAdapter.getSelectedPhotos().size());
                }
                if (isEnable) {
                    final List<Integer> positionList = new ArrayList<>(photoGridAdapter.getSelectedPhotosPosition());
                    photoGridAdapter.toggleSelection(photo, selectedPhotoPosition);

                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, photoGridAdapter.getSelectedItemCount());
                    photoGridAdapter.notifySelectedItemsChanged(position, positionList);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return photoGridAdapter.selectedPhotos.size();
    }

    public static class SelectedPhotoViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private ImageView ivCancel;

        public SelectedPhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.iv_photo);
            ivCancel = (ImageView) itemView.findViewById(R.id.iv_cancel);
        }
    }
}
