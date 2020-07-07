package com.jinu.imagepickerlib.adapter;

import com.jinu.imagepickerlib.entity.Photo;
import com.jinu.imagepickerlib.entity.PhotoDirectory;
import com.jinu.imagepickerlib.event.Selectable;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public abstract class SelectableAdapter<VH extends RecyclerView.ViewHolder>
    extends RecyclerView.Adapter<VH> implements Selectable {

  private static final String TAG = SelectableAdapter.class.getSimpleName();

  protected List<PhotoDirectory> photoDirectories;
  protected List<Photo> selectedPhotos;
  protected List<Integer> selectedPhotoPositions;
  public int currentDirectoryIndex = 0;


  public SelectableAdapter() {
    photoDirectories = new ArrayList<>();
    selectedPhotos = new ArrayList<>();
    selectedPhotoPositions = new ArrayList<>();
  }


  /**
   * Indicates if the item at position position is selected
   *
   * @param photo Photo of the item to check
   * @return true if the item is selected, false otherwise
   */
  @Override
  public boolean isSelected(Photo photo) {
    return getSelectedPhotos().contains(photo);
  }


  /**
   * Toggle the selection status of the item at a given position
   *
   * @param photo Photo of the item to toggle the selection status for
   */
  @Override
  public boolean toggleSelection(Photo photo, int position) {
    if (selectedPhotos.contains(photo)) {
      selectedPhotos.remove(photo);
      selectedPhotoPositions.remove(Integer.valueOf(position));

      return false;
    }

    selectedPhotos.add(photo);
    selectedPhotoPositions.add(position);

    return true;
  }


  /**
   * Clear the selection status for all items
   */
  @Override
  public void clearSelection() {
    selectedPhotos.clear();
    selectedPhotoPositions.clear();
  }


  /**
   * Count the selected items
   *
   * @return Selected items count
   */
  @Override
  public int getSelectedItemCount() {
    return selectedPhotos.size();
  }


  public void setCurrentDirectoryIndex(int currentDirectoryIndex) {
    this.currentDirectoryIndex = currentDirectoryIndex;
  }


  public List<Photo> getCurrentPhotos() {
    return photoDirectories.get(currentDirectoryIndex).getPhotos();
  }


  public List<String> getCurrentPhotoPaths() {
    List<String> currentPhotoPaths = new ArrayList<>(getCurrentPhotos().size());
    for (Photo photo : getCurrentPhotos()) {
      currentPhotoPaths.add(photo.getPath());
    }
    return currentPhotoPaths;
  }


  @Override
  public List<Photo> getSelectedPhotos() {
    return selectedPhotos;
  }

  public int getSelectedPhotoIndex(Photo photo) {
    return selectedPhotos.indexOf(photo);
  }

}