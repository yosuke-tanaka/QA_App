package jp.techacademy.tanaka.yousuke.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;
    private QuestionDetailListAdapter mAdapter;

    private DatabaseReference mAnswerRef;

    // plusボタン
    private FloatingActionButton mFab;

    // お気に入りボタン
    private FloatingActionButton mFab2;

    // お気に入りボタンの押下状態
    private boolean m_isFaboriteOn = false;

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            String answerUid = dataSnapshot.getKey();

            for(Answer answer : mQuestion.getAnswers()) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    /**
     * onCreateメソッドでは渡ってきたQuestionクラスのインスタンスを保持し、タイトルを設定します。そして、ListViewの準備をします。
     * FABをタップしたらログインしていなければログイン画面に遷移させ、
     * ログインしていれば後ほど作成する回答作成画面に遷移させる準備をしておきます。
     * そして重要なのがFirebaseへのリスナーの登録です。
     * 回答作成画面から戻ってきた時にその回答を表示させるために登録しておきます。
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // Questionを渡して回答作成画面を起動する
                    // QuestionDetailActivityからAnswerSendActivityに遷移するように修正
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        // 2016.09.20 [修正] お気に入り追加
        mFab2 = (FloatingActionButton) findViewById(R.id.fab_favorite);
        mFab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetFavoriteButton(m_isFaboriteOn);
            }
        });

        // お気に入りボタンはログイン済みの場合のみ表示する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null)
        {
            mFab2.hide();
        }

        // Questionのお気に入り状態に応じて、画面のお気に入りボタンの状態を設定
        String qUid = mQuestion.getUid();
        m_isFaboriteOn = getIsFavorite(qUid);
        SetFavoriteButton(m_isFaboriteOn);

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        mAnswerRef.addChildEventListener(mEventListener);
    }

    /**
     * お気に入り状態の切り替え
     * @param isFaboriteOn
     */
    private void SetFavoriteButton(boolean isFaboriteOn)
    {
        if(isFaboriteOn == true){
            m_isFaboriteOn = false;
            int color = Color.rgb(240,200,100);
            mFab2.setBackgroundTintList(ColorStateList.valueOf(color));
        }
        else
        {
            m_isFaboriteOn = true;
            int color = Color.rgb(200,200,200);
            mFab2.setBackgroundTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * お気に入り質問かどうかの情報をFireBaseから取得
     * @param questionUid
     * @return
     */
    private boolean getIsFavorite(String questionUid)
    {
        boolean isFavorite = true;

        // 現在ログイン中のUserに対応するChildの参照を取得
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference fquidRef = dataBaseReference.child(Const.UsersPATH).child(userUid).child(Const.FavoQUid);

        data = fquidRef.getKey();

        return isFavorite;
    }

//    /**
//     * お気に入り質問かどうかの情報をPreferenceから取得
//     * @param questionUid
//     * @return
//     */
//    private boolean getIsFavorite(String questionUid)
//    {
//        boolean isFavorite;
//        String qUid;
//        Object obj;
//
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//
//        // 読み込んでSetの末尾に追加
//        Set<String> uidSet = new HashSet<>();
//        sp.getStringSet(Const.FavoQUid, uidSet);
//        Iterator iterator = uidSet.iterator();
//        obj = iterator.next();
//
//        isFavorite =false;
//        while(obj != null){
//            qUid = (String)obj;
//            if(qUid == questionUid)
//            {
//                // お気に入り質問である
//                isFavorite = true;
//                break;
//            }
//        }
//
//        return isFavorite;
//    }

    /**
     //     * お気に入り質問をFireBaseに保存
     //     * [参考] http://qiita.com/piruty_joy/items/21aa5557ec380e93599e
     //     * @param qUid
     //     */
    private void saveFavoriteQuestionUid(String qUid) {
        // 現在ログイン中のUserに対応するChildの参照を取得
        String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = dataBaseReference.child(Const.UsersPATH).child(userUid);

        Map<String, String> data = new HashMap<String, String>();
        data.put(Const.FavoQUid, qUid);

        userRef.push().setValue(data, this);
        //mProgress.show();
    }

//    /**
//     * お気に入り質問をPrefernceに保存
//     * [参考] http://qiita.com/piruty_joy/items/21aa5557ec380e93599e
//     * @param uid
//     */
//    private void saveFavoriteQuestionUid(String uid) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//
//        // 読み込んでSetの末尾に追加
//        Set<String> uidSet = new HashSet<>();
//        sp.getStringSet(Const.FavoQUid, uidSet);
//        uidSet.add(uid);
//        // 保存
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putStringSet(Const.FavoQUid, uidSet);
//        editor.commit();
//    }

}