package com.mediatek.wwtv.tvcenter.search;

import android.content.Context;
import android.content.Intent;
import android.media.tv.TvContract.Programs;
import android.net.Uri;
import android.util.Log;

import com.mediatek.wwtv.tvcenter.search.LocalSearchProvider.SearchResult;
import com.mediatek.wwtv.tvcenter.R;

import java.util.ArrayList;
import java.util.List;


public final class QuestionSearch implements SearchInterface {
    private static final String TAG = "QuestionSearch";

    /*private static final String[] mQuestions =
        {"what's on tv",
         "what is on tv"};*/

    private final Context mContext;

    QuestionSearch(Context context) {
        mContext = context;
    }

    /**
     * Search channels, inputs, or programs from TvProvider.
     * This assumes that parental control settings will not be change while searching.
     *
     * @param action One of {@link #ACTION_TYPE_SWITCH_CHANNEL}, {@link #ACTION_TYPE_SWITCH_INPUT},
     *               or {@link #ACTION_TYPE_AMBIGUOUS},
     */
    @Override
    public List<SearchResult> search(String query, int limit, int action) {
        List<SearchResult> results = new ArrayList<>();

		Log.d(TAG, "query:" + query + ", " + limit + ", " + action);

        if(query == null) {
            return results;
        }

        String[] mQuestions ={"what's on tv", "what is on tv"};
        for(String str : mQuestions) {
            if(query.contains(str)){
                return add(results);
            }
        }

        return results;
    }

    private List<SearchResult> add(List<SearchResult> list) {

        SearchResult result = new SearchResult();
        result.channelId = 0L;
        result.channelNumber = "1";
        result.title = mContext.getResources().getString(R.string.menu_tv_channels);
        result.description = mContext.getResources().getString(R.string.channels_item_program_guide);
        result.imageUri = Uri.parse("android.resource://com.mediatek.wwtv.tvcenter/drawable/icon").toString();
        result.intentAction = Intent.ACTION_VIEW;
        result.intentData = Programs.CONTENT_URI.toString();
        result.contentType = Programs.CONTENT_ITEM_TYPE;
        result.isLive = true;
        result.progressPercentage = LocalSearchProvider.PROGRESS_PERCENTAGE_HIDE;

        list.add(result);

        return list;
    }
}
