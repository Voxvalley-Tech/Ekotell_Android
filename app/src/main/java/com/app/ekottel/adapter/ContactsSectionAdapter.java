package com.app.ekottel.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.app.ekottel.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;


/**
 * Adapter for sections.
 */

/**
 * This activity is used to display Sections.
 *
 * @author Ramesh U
 * @version 2017
 */
public class ContactsSectionAdapter extends BaseAdapter implements ListAdapter {
    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateSessionCache();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            updateSessionCache();
        }


    };

    private final ListAdapter linkedAdapter;
    private final Map<Integer, String> sectionPositions = new LinkedHashMap<Integer, String>();
    private final Map<Integer, Integer> itemPositions = new LinkedHashMap<Integer, Integer>();
    private final Map<View, String> currentViewSections = new HashMap<View, String>();
    private int viewTypeCount;
    protected final LayoutInflater inflater;
    private Context mContext;
    private ArrayList<String> contactsArray;


    public ContactsSectionAdapter(Context context, final LayoutInflater inflater,
                                  final ListAdapter linkedAdapter, ArrayList<String> contactsarray) {
        this.linkedAdapter = linkedAdapter;
        this.inflater = inflater;
        this.contactsArray = contactsarray;
        linkedAdapter.registerDataSetObserver(dataSetObserver);
        updateSessionCache();
        mContext = context;
    }

    private boolean isTheSame(final String previousSection,
                              final String newSection) {
        if (previousSection == null) {
            return newSection == null;
        } else {
            return previousSection.equals(newSection);
        }
    }

    private synchronized void updateSessionCache() {
        int currentPosition = 0;
        sectionPositions.clear();
        itemPositions.clear();
        viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
        String currentSection = null;
        final int count = linkedAdapter.getCount();

        for (int i = 0; i < count; i++) {

            final String item = contactsArray.get(i).toString().substring(0, 1).toUpperCase(Locale.ENGLISH);

            if (!isTheSame(currentSection, item)) {
                sectionPositions.put(currentPosition, item);
                currentSection = item;
                currentPosition++;
            }

            itemPositions.put(currentPosition, i);
            currentPosition++;
        }

    }

    @Override
    public synchronized int getCount() {
        return sectionPositions.size() + itemPositions.size();
    }

    @Override
    public synchronized Object getItem(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position);
        } else {
            final int linkedItemPosition = getLinkedPosition(position);
            return linkedAdapter.getItem(linkedItemPosition);
        }
    }

    public synchronized boolean isSection(final int position) {
        return sectionPositions.containsKey(position);
    }

    @Override
    public long getItemId(final int position) {
        if (isSection(position)) {
            return sectionPositions.get(position).hashCode();
        } else {
            return linkedAdapter.getItemId(getLinkedPosition(position));
        }
    }

    private Integer getLinkedPosition(final int position) {
        return itemPositions.get(position);
    }

    @Override
    public int getItemViewType(final int position) {
        if (isSection(position)) {
            return viewTypeCount - 1;
        }
        return linkedAdapter.getItemViewType(getLinkedPosition(position));
    }

    private View getSectionView(final View convertView, final String section) {
        View theView = convertView;
        if (theView == null) {
            theView = createNewSectionView();
        }
        setSectionText(section, theView);
        replaceSectionViewsInMaps(section, theView);
        return theView;
    }

    private void setSectionText(final String section, final View sectionView) {
        LinearLayout ll_zone_item = (LinearLayout) sectionView
                .findViewById(R.id.ll_zone_item);


        TextView textView = (TextView) sectionView
                .findViewById(R.id.listTextView);

        textView.setEnabled(false);
        textView.setText(section);
        ll_zone_item.setOnClickListener(null);
    }

    private synchronized void replaceSectionViewsInMaps(final String section,
                                                          final View theView) {
        if (currentViewSections.containsKey(theView)) {
            currentViewSections.remove(theView);
        }
        currentViewSections.put(theView, section);
    }

    private View createNewSectionView() {
        return inflater.inflate(R.layout.contacts_section_adapter, null);
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {
        if (isSection(position)) {
            return getSectionView(convertView, sectionPositions.get(position));
        }
        return linkedAdapter.getView(getLinkedPosition(position), convertView,
                parent);
    }

    @Override
    public int getViewTypeCount() {
        return viewTypeCount;
    }

    @Override
    public boolean hasStableIds() {
        return linkedAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return linkedAdapter.isEmpty();
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        linkedAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return linkedAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEnabled(final int position) {
        if (isSection(position)) {
            return true;
        }
        return linkedAdapter.isEnabled(getLinkedPosition(position));
    }


}
