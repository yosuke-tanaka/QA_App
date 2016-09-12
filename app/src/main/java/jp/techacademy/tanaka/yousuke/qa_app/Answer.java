package jp.techacademy.tanaka.yousuke.qa_app;

import java.io.Serializable;

/**
 * 質問の回答のモデルクラスであるAnswer.java
 * メンバ変数を以下のように作成し、それぞれGetterを用意します。
 * 値はコンストラクタで設定するのみでSetterは作りません

 変数名	内容
 mBody	Firebaseから取得した回答本文
 mName	Firebaseから取得した回答者の名前
 mUid	Firebaseから取得した回答者のUID
 mQuestionUid	Firebaseから取得した回答のUID
 */
public class Answer implements Serializable {
    private String mBody;
    private String mName;
    private String mUid;
    private String mAnswerUid;

    public Answer(String body, String name, String uid, String answerUid) {
        mBody = body;
        mName = name;
        mUid = uid;
        mAnswerUid = answerUid;
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

    public String getAnswerUid() {
        return mAnswerUid;
    }
}