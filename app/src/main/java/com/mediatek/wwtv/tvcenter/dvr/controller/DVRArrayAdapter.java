/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.wwtv.tvcenter.dvr.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.mediatek.wwtv.tvcenter.R;

import android.content.Context;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A concrete BaseAdapter that is backed by an array of arbitrary objects. By
 * default this class expects that the provided resource id references a single
 * TextView. If you want to use a more complex layout, use the constructors that
 * also takes a field id. That field id should reference a TextView in the
 * larger layout resource.
 *
 * <p>
 * However the TextView is referenced, it will be filled with the toString() of
 * each object in the array. You can add lists or arrays of custom objects.
 * Override the toString() method of your objects to determine what text will be
 * displayed for the item in the list.
 *
 * <p>
 * To use something other than TextViews for the array display, for instance,
 * ImageViews, or to have some of data besides toString() results fill the
 * views, override {@link #getView(int, View, ViewGroup)} to return the type of
 * view you want.
 */
public class DVRArrayAdapter<T> extends BaseAdapter implements Filterable {
	/**
	 * Contains the list of objects that represent the data of this
	 * ArrayAdapter. The content of this list is referred to as "the array" in
	 * the documentation.
	 */
	private List<T> mObjects;

	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation
	 * performed on the array should be synchronized on this lock. This lock is
	 * also used by the filter (see {@link #getFilter()} to make a synchronized
	 * copy of the original array of data.
	 */
	private final Object mLock = new Object();

	/**
	 * The resource indicating what views to inflate to display the content of
	 * this array adapter.
	 */
	private int mResource;

	/**
	 * The resource indicating what views to inflate to display the content of
	 * this array adapter in a drop down widget.
	 */
	private int mDropDownResource;


	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called
	 * whenever {@link #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;

	private Context mContext;

	private int subStartIndex =0;

	private int currenSelect = 0;



	public List<T> getmObjects() {
		return mObjects;
	}

	public void setmObjects(List<T> mObjects) {
		this.mObjects = mObjects;
	}

	public int getCurrenSelect() {
		return currenSelect;
	}

	public void setCurrenSelect(int currenSelect) {
		this.currenSelect = currenSelect;
	}

	public int getSubStartIndex() {
		return subStartIndex;
	}

	public void setSubStartIndex(int subStartIndex) {
		this.subStartIndex = subStartIndex;
	}

	// A copy of the original mObjects array, initialized from and then used
	// instead as soon as
	// the mFilter ArrayFilter is used. mObjects will then only contain the
	// filtered values.
	private  List<T> mOriginalValues;
	private ArrayFilter mFilter;

	private LayoutInflater mInflater;

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 */
	public DVRArrayAdapter(Context context, int textViewResourceId) {
		init(context, textViewResourceId, 0, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 */
	public DVRArrayAdapter(Context context, int resource, int textViewResourceId) {
		init(context, resource, textViewResourceId, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public DVRArrayAdapter(Context context, int textViewResourceId, T[] objects) {
		init(context, textViewResourceId, 0, Arrays.asList(objects));
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public DVRArrayAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		init(context, resource, textViewResourceId, Arrays.asList(objects));
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param textViewResourceId
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public DVRArrayAdapter(Context context, int textViewResourceId,
			List<T> objects,int subStartIndex,int currenSelect) {
		this.subStartIndex = subStartIndex;
		this.currenSelect = currenSelect;
		init(context, textViewResourceId, 0, objects);
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public DVRArrayAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		init(context, resource, textViewResourceId, objects);
	}

	/**
	 * Adds the specified object at the end of the array.
	 *
	 * @param object
	 *            The object to add at the end of the array.
	 */
	public void add(T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
			} else {
				mObjects.add(object);
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * Adds the specified Collection at the end of the array.
	 *
	 * @param collection
	 *            The Collection to add at the end of the array.
	 */
	public void addAll(Collection<? extends T> collection) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.addAll(collection);
			} else {
				mObjects.addAll(collection);
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * Adds the specified items at the end of the array.
	 *
	 * @param items
	 *            The items to add at the end of the array.
	 */
	public void addAll(T... items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.addAll(mOriginalValues, items);
			} else {
				Collections.addAll(mObjects, items);
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * Inserts the specified object at the specified index in the array.
	 *
	 * @param object
	 *            The object to insert into the array.
	 * @param index
	 *            The index at which the object must be inserted.
	 */
	public void insert(T object, int index) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(index, object);
			} else {
				mObjects.add(index, object);
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * Removes the specified object from the array.
	 *
	 * @param object
	 *            The object to remove.
	 */
	public void remove(T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.remove(object);
			} else {
				mObjects.remove(object);
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * Remove all elements from the list.
	 */
	public void clear() {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.clear();
			} else {
				mObjects.clear();
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * Sorts the content of this adapter using the specified comparator.
	 *
	 * @param comparator
	 *            The comparator used to sort the objects contained in this
	 *            adapter.
	 */
	public void sort(Comparator<? super T> comparator) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.sort(mOriginalValues, comparator);
			} else {
				Collections.sort(mObjects, comparator);
			}
		}
		if (mNotifyOnChange){
			notifyDataSetChanged();
          }
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Control whether methods that change the list ({@link #add},
	 * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
	 * {@link #notifyDataSetChanged}. If set to false, caller must manually call
	 * notifyDataSetChanged() to have the changes reflected in the attached
	 * view.
	 *
	 * The default is true, and calling notifyDataSetChanged() resets the flag
	 * to true.
	 *
	 * @param notifyOnChange
	 *            if true, modifications to the list will automatically call
	 *            {@link #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}

	private void init(Context context, int resource, int textViewResourceId,
			List<T> objects) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = mDropDownResource = resource;
		mObjects = objects;
		android.util.Log.d("DVRArray", "id=="+textViewResourceId);
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 *
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public int getCount() {
        try {
			if(mObjects !=null ){
				return mObjects.size();
			}else{
				return 0;
			}
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public T getItem(int position) {
        try {
            if (mObjects != null) {
                return mObjects.get(position);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

	}

	/**
	 * Returns the position of the specified item in the array.
	 *
	 * @param item
	 *            The item to retrieve the position of.
	 *
	 * @return The position of the specified item.
	 */
	public int getPosition(T item) {
		return mObjects.indexOf(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public long getItemId(int position) {
		return position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(resource, parent, false);
			holder = new ViewHolder();
			holder.layout = (LinearLayout)convertView.findViewById(R.id.layout);
			holder.textview = (TextView)convertView.findViewById(R.id.record_item);
			holder.textViewTime = (TextView)convertView.findViewById(R.id.record_item_time);
			holder.textViewDuration = (TextView)convertView.findViewById(R.id.record_item_duration);
			convertView.setTag(holder);
		} else {
			holder  = (ViewHolder)convertView.getTag();
		}

		/*if (position == currenSelect) {
			holder.layout.setBackgroundResource(R.drawable.nav_ch_list);
		} else {
			holder.layout.setBackgroundColor(Color.TRANSPARENT);
		}
*/
		DVRFiles item = (DVRFiles) getItem(position);
		if (item != null) {
            if (item instanceof CharSequence) {
                holder.textview.setText((CharSequence) item);
            } else {
//                String name = String.format("[%2d] %s", (subStartIndex + position),
//                        item.getProgramName());
				StringBuilder content = 	new StringBuilder();
                if(!item.getmDetailInfo().isEmpty()){
					content.append(item.getChannelName())
							.append("_")
							.append(item.getDate().replace("/", ""))
							.append("_")
							.append(item.getmDetailInfo());
				}else{
					content.append(item.getChannelName()).append("_").append(item.getDate().replace("/", ""));
				}
				holder.textview.setText(content.toString()
						);

				holder.textViewTime.setText(item.getTime());
                holder.textViewDuration.setText(item.getDurationStr());
            }

            if (item.isRecording || item.isPlaying) {
                if (item.isRecording) {
                    holder.textview.setCompoundDrawablesRelativeWithIntrinsicBounds(mContext
                            .getResources().getDrawable(R.drawable.pvr_record_rec),
                            null, null, null);
                }

                if (item.isPlaying) {
                    holder.textview.setCompoundDrawablesRelativeWithIntrinsicBounds(mContext
                            .getResources().getDrawable(R.drawable.timeshift_play),
                            null, null, null);
                }
            } else {
                holder.textview.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null,
                        null, null);
            }
		}

		return convertView;
	}


	class ViewHolder {

		private LinearLayout layout;

		private TextView  textview;
        private TextView textViewTime;
        private TextView textViewDuration;


	}

	/**
	 * <p>
	 * Sets the layout resource to create the drop down views.
	 * </p>
	 *
	 * @param resource
	 *            the layout resource defining the drop down views
	 * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
	 */
	public void setDropDownViewResource(int resource) {
		this.mDropDownResource = resource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent,
				mDropDownResource);
	}

	/**
	 * Creates a new ArrayAdapter from external resources. The content of the
	 * array is obtained through
	 * {@link android.content.res.Resources#getTextArray(int)}.
	 *
	 * @param context
	 *            The application's environment.
	 * @param textArrayResId
	 *            The identifier of the array to use as the data source.
	 * @param textViewResId
	 *            The identifier of the layout used to create views.
	 *
	 * @return An ArrayAdapter<CharSequence>.
	 */
	public static DVRArrayAdapter<CharSequence> createFromResource(
			Context context, int textArrayResId, int textViewResId) {
		CharSequence[] strings = context.getResources().getTextArray(
				textArrayResId);
		return new DVRArrayAdapter<CharSequence>(context, textViewResId,
				strings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
    public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	/**
	 * <p>
	 * An array filter constrains the content of the array adapter with a
	 * prefix. Each item that does not start with the supplied prefix is removed
	 * from the list.
	 * </p>
	 */
	private class ArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			synchronized (mLock) {
				if (mOriginalValues == null) {
						mOriginalValues = new ArrayList<T>(mObjects);
				}
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<T> list;
				synchronized (mLock) {
					list = new ArrayList<T>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {
				String prefixString = String.valueOf(prefix).toLowerCase(Locale.ROOT);

				ArrayList<T> values;
				synchronized (mLock) {
					values = new ArrayList<T>(mOriginalValues);
				}

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<T>();

				for (int i = 0; i < count; i++) {
					final T value = values.get(i);
					final String valueText = String.valueOf(value).toLowerCase(Locale.ROOT);

					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString)) {
						newValues.add(value);
					} else {
						final String[] words = valueText.split(" ");
						final int wordCount = words.length;

						// Start at index 0, in case valueText starts with
						// space(s)
						for (int k = 0; k < wordCount; k++) {
							if (words[k].startsWith(prefixString)) {
								newValues.add(value);
								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mObjects = (List<T>) results.values;
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}
}
