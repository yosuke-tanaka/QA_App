package jp.techacademy.tanaka.yousuke.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class FavoriteQuestionActivity extends AppCompatActivity {
    // メンバ変数としてFirebaseへのアクセスに必要なDatabaseReferenceクラスと、ListView、QuestionクラスのArrayList、QuestionsListAdapterを定義
    private DatabaseReference mDatabaseReference;

    private DatabaseReference mQRef1;
    private DatabaseReference mQRef2;
    private DatabaseReference mQRef3;
    private DatabaseReference mQRef4;


    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    /**
     * データに追加・変化があった時に受け取るChildEventListenerを作成
     */
    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        /**
         * onChildAddedメソッドが要素が追加されたとき、つまり質問が追加された時に呼ばれるメソッドです。
         * この中でQuestionクラスとAnswerを作成し、ArrayListに追加します。
         *
         * Prefernceに登録されているお気に入りQUID情報を利用して、お気に入り質問のみを表示するようにする
         */
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // Prefernceに登録されているお気に入りQUID情報を利用して、お気に入り質問のみを表示するようにする
            String questionUid = dataSnapshot.getKey();
            boolean isFavorite = getIsFavorite(questionUid);
            if(isFavorite == false)
            {
               return;
            }

            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            Bitmap image = null;
            byte[] bytes;
            if (imageString != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            int xxx = 0;
            Question question = new Question(title, body, name, uid, questionUid, xxx, bytes, answerArrayList);
            mQuestionArrayList.add(question);
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_question);

        // UIの準備
        setTitle("★お気に入り質問");

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear();
        mAdapter.setQuestionArrayList(mQuestionArrayList);
        mListView.setAdapter(mAdapter);

        // 選択したジャンルにリスナーを登録する
        // [注意] すべてのジャンルを表示する必要がある
        mQRef1 = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(1));
        mQRef1.addChildEventListener(mEventListener);

        mQRef2 = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(2));
        mQRef2.addChildEventListener(mEventListener);

        mQRef3 = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(3));
        mQRef3.addChildEventListener(mEventListener);

        mQRef4 = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(4));
        mQRef4.addChildEventListener(mEventListener);


//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                // Questionのインスタンスを渡して質問詳細画面を起動する
//                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
//                intent.putExtra("question", mQuestionArrayList.get(position));
//                startActivity(intent);
//            }
//        });

    }


    /**
      * お気に入り質問かどうかの情報をPreferenceから取得
      * @param questionUid
      * @return
      */
    private boolean getIsFavorite(String questionUid)
    {
        boolean isFavorite;
        String qUid;
        Object obj;

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // 読み込んでSetの末尾に追加
        Set<String> uidSet = new HashSet<>();
        sp.getStringSet(Const.FavoQUid, uidSet);

        isFavorite = uidSet.contains(questionUid);

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

        return isFavorite;
    }

}
