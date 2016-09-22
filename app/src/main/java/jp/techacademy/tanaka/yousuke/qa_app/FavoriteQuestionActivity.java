package jp.techacademy.tanaka.yousuke.qa_app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FavoriteQuestionActivity extends AppCompatActivity {
    // メンバ変数としてFirebaseへのアクセスに必要なDatabaseReferenceクラスと、ListView、QuestionクラスのArrayList、QuestionsListAdapterを定義
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_question);

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

//        // 選択したジャンルにリスナーを登録する
//        if (mGenreRef != null) {
//            mGenreRef.removeEventListener(mEventListener);
//        }
//        mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
//        mGenreRef.addChildEventListener(mEventListener);


//        // QuestionDetailActivityの実装が完了したら質問一覧画面でリストをタップしたらその質問の詳細画面に飛ぶように修正します。
//        // ListViewのsetOnItemClickListenerメソッドでリスナーを登録し、リスナーの中で質問に相当するQuestionのインスタンスを渡してQuestionDetailActivityに遷移させます。
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

}
