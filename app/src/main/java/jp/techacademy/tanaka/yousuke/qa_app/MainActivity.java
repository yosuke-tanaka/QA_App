package jp.techacademy.tanaka.yousuke.qa_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *修正する内容は以下の通りです。

 ・ドロワーを表示するためのボタンを表示する
 ・ドロワー内のメニューが選択された時にどのメニューが選択されたかを保持する
 ・ドロワー内のメニューが選択された時にタイトルを設定する

 メンバ変数としてmToolbarとmGenreを定義します。
 */
public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenre = 0;

    // メンバ変数としてFirebaseへのアクセスに必要なDatabaseReferenceクラスと、ListView、QuestionクラスのArrayList、QuestionsListAdapterを定義
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    //private DatabaseReference mFavoRef;

    private Button mFavoButton;
    private View mNaviHeader;

    NavigationView mNavigationView;

    /**
     * データに追加・変化があった時に受け取るChildEventListenerを作成
     */
    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        /**
         * onChildAddedメソッドが要素が追加されたとき、つまり質問が追加された時に呼ばれるメソッドです。
         * この中でQuestionクラスとAnswerを作成し、ArrayListに追加します。
         */
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();
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

            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            mQuestionArrayList.add(question);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        /**
         * onChildChangedメソッドは要素に変化があった時です。
         * 今回は質問に対して回答が投稿された時に呼ばれることとなります。
         * このメソッドが呼ばれたら変化があった質問に対応するQuestionクラスのインスタンスが保持している回答のArrayListを一旦クリアし、取得した回答を設定します。
         *
         * // このアプリで変更がある可能性があるのは：
         * 　・回答(Answer)
         */
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
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
     * onCreateメソッドでfindViewByIdを使って取得している箇所をmToolbarを使うように修正します。
     そしてドロワーの設定を追加します。
     修正したら、実行してドロワーが表示され、メニューを選択するとタイトルが変更されることを確認しましょう。
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_favorite) {
                    // ログイン済みのユーザーを取得する
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user == null) {
                        // ログインしていなければログイン画面に遷移させる
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);
                    } else {
                        // お気に入り画面を表示する
                        Intent intent = new Intent(getApplicationContext(), FavoriteQuestionActivity.class);
                        startActivity(intent);
                    }

                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    drawer.closeDrawer(GravityCompat.START);

                    return true;
                }

                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenre = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                } else if (id == R.id.nav_compter) {
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);


                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                mQuestionArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);

                // 選択したジャンルにリスナーを登録する
                // ドロワーでジャンルが選択された時に、Firebaseに対してそのジャンルの質問のデータの変化を受け取るように先ほど作成したChildEventListenerを設定します。
                if (mGenreRef != null) {
                    mGenreRef.removeEventListener(mEventListener);
                }
                mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                mGenreRef.addChildEventListener(mEventListener);

                return true;
            }
        });

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        // QuestionDetailActivityの実装が完了したら質問一覧画面でリストをタップしたらその質問の詳細画面に飛ぶように修正します。
        // ListViewのsetOnItemClickListenerメソッドでリスナーを登録し、リスナーの中で質問に相当するQuestionのインスタンスを渡してQuestionDetailActivityに遷移させます。
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });

        // 2016.09.28 [修正] FireBaseからお気に入りQUID情報を取得しSharedPrefernceに保存
        //(端末が変わった場合、SharedPrefernceにQUID情報情報がないので)
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            DatabaseReference mFavoRef = mDatabaseReference.child(Const.UsersPATH).child(user.getUid());
            mFavoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Map data = (Map) snapshot.getValue();
                    Global.saveFavoriteQuestionUid((HashMap) data.get(Const.FavoQUid));
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                }
            });
        }

        // お気に入り画面に移動ボタン
        //mFavoButton = (Button) navigationView.getHeaderView(0).findViewById(R.id.buttonFavoList);
        //mFavoButton.setText("test");

        // [memo]hCnt = 1になる
        //int hCnt  = navigationView.getHeaderCount();  // メニュー内容の個数
        //mNaviHeader = navigationView.getHeaderView(hCnt - 1);

        // [memo]mFavoButton = nullになる
        //mNaviHeader = navigationView.getHeaderView(0);
        //mFavoButton = (Button) mNaviHeader.findViewById(R.id.buttonFavoList);

//        mFavoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // お気に入り画面を表示する
//                Intent intent = new Intent(getApplicationContext(), FavoriteQuestionActivity.class);
//                startActivity(intent);
//            }
//        });

    }

    @Override
    /**
     * ドロワー内のお気に入りボタンの表示切替
     * [注意] 設定画面でログアウトされた後にも更新したいのでonResume内で行う
     */
    protected void onResume(){
        super.onResume();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // 2016.10.04 [修正] お気に入りへ移動項目の表示切替
        MenuItem item = mNavigationView.getMenu().getItem(0);


        // ログイン時のみ表示
        if(user == null) {
            //mFavoButton.setVisibility(View.INVISIBLE);
            //mNaviHeader.setVisibility(View.GONE);

            item.setVisible(false);
        }
        else
        {
            //mFavoButton.setVisibility(View.VISIBLE);
            //mNaviHeader.setVisibility(View.VISIBLE);

            item.setVisible(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


//    /**
//     * お気に入り情報をPreferenceに保存
//     * @param fqUids お気に入りQUID一覧
//     */
//    private void saveFavoriteQuestionUid(HashMap fqUids) {
//        // Preferenceに保存する
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//
//        // 読み込んでSetの末尾に追加
//        Set<String> uidSet = new HashSet<>();
//        for (Object key : fqUids.keySet()) {
//            uidSet.add((String)key);
//        }
//
//        // 保存
//        SharedPreferences.Editor editor = sp.edit();
//        editor.putStringSet(Const.FavoQUid, uidSet);
//        editor.commit();
//    }

}