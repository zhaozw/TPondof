package fish.pondof.tpondof.api;

import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fish.pondof.tpondof.api.model.Discussion;
import fish.pondof.tpondof.util.NetworkUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static fish.pondof.tpondof.BuildConfig.DEBUG;

/**
 * Created by Administrator on 2017/2/22.
 * @author Trumeet
 */

public class DiscussionListManager {
    private static final String TAG = "DiscussionListManager";
    public static List<Discussion> getList (boolean useCache) throws APIException {
        if (DEBUG) Log.i(TAG, "-> getList");
        List<Discussion> list = new ArrayList<>();
        try {
            JSONObject rootObject = JSONObject.parseObject(NetworkUtil
                    .get(ApiManager.API_DISCUSSIONS, useCache));
            if (DEBUG) Log.i(TAG, "Json Parsed");
            JSONArray dataArray = rootObject.getJSONArray("data");
            if (DEBUG) Log.i(TAG, "Data Size:" + dataArray.size());
            JSONArray included = rootObject.getJSONArray("included");
            if (DEBUG) Log.i(TAG, "Included Size:" + included.size());
            for (int i = 0; i < included.size(); i ++) {
                JSONObject object = included.getJSONObject(i);
                String type = object.getString("type");
                if (type.equals("users")) {
                    if (DEBUG) Log.i(TAG, "Get User");
                    // Just cache it
                    UserItemManager.getUserInfo(object.getInteger("id"), object, false);
                } else if (type.equals("tags")) {
                    if (DEBUG) Log.i(TAG, "Get Tag");
                    // Cache it
                    TagManager.getTag(object.getInteger("id"), object);
                }
            }
            for (int i = 0; i < dataArray.size(); i ++) {
                JSONObject object = dataArray.getJSONObject(i);
                JSONObject attributes = object.getJSONObject("attributes");
                Discussion discussion = new Discussion();
                discussion.setID(object.getInteger("id"));
                discussion.setTitle(attributes.getString("title"));
                discussion.setSlug(attributes.getString("slug"));
                discussion.setApproved(attributes.getBoolean("isApproved"));
                discussion.setCanDelete(attributes.getBoolean("canDelete"));
                discussion.setCanHide(attributes.getBoolean("canHide"));
                discussion.setCanLock(attributes.getBoolean("canLock"));
                discussion.setCanSticky(attributes.getBoolean("canSticky"));
                discussion.setCanRename(attributes.getBoolean("canRename"));
                discussion.setCanTag(attributes.getBoolean("canTag"));
                discussion.setCommentsCount(attributes.getInteger("commentsCount"));
                discussion.setStartTime(attributes.getString("startTime"));
                discussion.setSubscription(attributes.getString("subscription"));
                discussion.setVingleShareSocial(attributes.getString("vingle.share.social"));
                discussion.setParticipantsCount(attributes.getInteger("participantsCount"));
                discussion.setCanReply(attributes.getBoolean("canReply"));
                discussion.setLastTime(attributes.getString("lastTime"));
                discussion.setLastPostNumber(attributes.getInteger("lastPostNumber"));
                discussion.setContentHtml(attributes.getString("contentHtml"));

                JSONObject relationships = object.getJSONObject("relationships");
                JSONObject startUser = relationships.getJSONObject("startUser");
                JSONObject lastUser = relationships.getJSONObject("lastUser");
                JSONObject startPost = relationships.getJSONObject("startPost");
                JSONObject tags = relationships.getJSONObject("tags");

                // TODO: StartPost

                JSONArray tagsArray = tags.getJSONArray("data");
                List<Integer> t = discussion.getTags();
                for (int j = 0; j < tagsArray.size(); j ++) {
                    JSONObject tagObject = tagsArray.getJSONObject(j);
                    t.add(tagObject.getInteger("id"));
                }
                discussion.setTags(t);

                discussion.setAuthor(UserItemManager.getUserInfo(startUser
                        .getJSONObject("data").getInteger("id"), useCache));
                discussion.setLastUser(UserItemManager.getUserInfo(lastUser
                        .getJSONObject("data").getInteger("id"), useCache));

                if (DEBUG) Log.i(TAG, "Adding " + discussion.toString());
                list.add(discussion);
            }
        } catch (IOException|JSONException e) {
            if (DEBUG) Log.e(TAG, "Fetch List, fail");
            throw new APIException(e);
        }
        return list;
    }
}
