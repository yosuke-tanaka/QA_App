package jp.techacademy.tanaka.yousuke.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Firebaseから取得した質問のデータを保持するモデルクラスとしてQuestionクラスを作成します。
 * メンバ変数を以下のように作成し、それぞれGetterを用意します。値はコンストラクタで設定するのみでSetterは作りません。
 * Serializableクラスを実装している理由はIntentでデータを渡せるようにするためです。
 * このQuestionクラスの中で保持しているクラスもSerializableクラスを実装している必要があるため、後述のAnswerクラスもSerializableクラスを実装させます。
 *
 * 変数名	内容
 mTitle	Firebaseから取得したタイトル
 mBody	Firebaseから取得した質問本文
 mName	Firebaseから取得した質問者の名前
 mUid	Firebaseから取得した質問者のUID
 mQuestionUid	Firebaseから取得した質問のUID
 mGenre	質問のジャンル
 mBitmapArray	Firebaseから取得した画像をbyte型の配列にしたもの
 mAnswerArrayList	Firebaseから取得した質問のモデルクラスであるAnswerのArrayList
 */
public class Question implements Serializable {
    private String mTitle;
    private String mBody;
    private String mName;
    private String mUid;
    private String mQuestionUid;
    private int mGenre;
    private byte[] mBitmapArray;
    private ArrayList<Answer> mAnswerArrayList;

    // 2016.09.20 [修正] お気に入り追加
    private boolean mIsFavorite;

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }
    public int getGenre() {
        return mGenre;
    }

    public byte[] getImageBytes() {
        return mBitmapArray;
    }

    public ArrayList<Answer> getAnswers() {
        return mAnswerArrayList;
    }

    /**
     * 2016.09.20 [修正] お気に入り追加
     * @return
     */
    public boolean getIsFavorite() {
        return mIsFavorite;
    }
    public void setIsFavorite_byStr(String isFavorite) {
        if(isFavorite == "0") {
            mIsFavorite = false;
        }
        else
        {
            mIsFavorite = true;
        }
    }

    public Question(String title, String body, String name, String uid, String questionUid, int genre,
                    byte[] bytes, ArrayList<Answer> answers, String isFavoriteStr) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
        setIsFavorite_byStr(isFavoriteStr);
    }
}