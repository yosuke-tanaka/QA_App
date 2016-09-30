package jp.techacademy.tanaka.yousuke.qa_app;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Global変数.
 */
public class Global {

    public static Set<String> gFavoQUidSet = new HashSet<>();


    /**
     * お気に入り情報をグローバル変数に格納
     * @param fqUids
     */
    public static void saveFavoriteQuestionUid(HashMap fqUids) {

        for (Object key : fqUids.keySet()) {
            Global.gFavoQUidSet.add((String)key);
        }
    }

    /**
     * お気に入り質問をグローバル変数に1件追加/1件削除
     * [参考] http://qiita.com/piruty_joy/items/21aa5557ec380e93599e
     * @param qUid_in
     * @param isAdd 追加/削除
     */
    public static void updateFavoriteQuestionUid(String qUid_in, boolean isAdd) {

        if(isAdd == true) {
            // 1件追加
            gFavoQUidSet.add(qUid_in);
        }
        else
        {
            // 1件削除
            gFavoQUidSet.remove(qUid_in);
        }
    }

    /**
     * お気に入り質問か確認
     * @param questionUid
     * @return
     */
    public static boolean getIsFavorite(String questionUid)
    {
        boolean isFavorite;

        isFavorite = gFavoQUidSet.contains(questionUid);

        return isFavorite;
    }



}
