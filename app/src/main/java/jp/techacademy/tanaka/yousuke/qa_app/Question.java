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

    public Question(String title, String body, String name, String uid, String questionUid, int genre, byte[] bytes, ArrayList<Answer> answers) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
    }
}