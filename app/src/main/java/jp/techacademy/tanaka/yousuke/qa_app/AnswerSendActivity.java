package jp.techacademy.tanaka.yousuke.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * OnClickListenerとFirebaseのCompletionListenerを実装
 * implements View.OnClickListener, DatabaseReference.CompletionListener
 */
public class AnswerSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    //メンバ変数としてEditTextを保持する変数、Intentで渡ってきたQuestionを保持する変数、ダイアログを保持する変数を定義
    private EditText mAnswerEditText;
    private Question mQuestion;
    private ProgressDialog mProgress;

    @Override
    /**
     * onCreateメソッドでは渡ってきたQuestionのインスタンスを保持することと、UIの準備を行います。
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_send);

        // 渡ってきたQuestionのオブジェクトを保持する
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        // UIの準備
        mAnswerEditText = (EditText) findViewById(R.id.answerEditText);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");

        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }

    @Override
    /**
     * Firebaseへの書き込み完了を受け取るonCompleteメソッドでは成功したらfinishメソッドを呼び出してActivityを閉じ、失敗した場合はSnackbarでその旨を表示させます。
     */
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        mProgress.dismiss();

        if (databaseError == null) {
            finish();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }

    }

    @Override
    /**
     * 投稿ボタンを押した時に呼ばれるonClickメソッドではキーボードを閉じて、Firebaseに書き込みます
     */
    public void onClick(View v) {
        // キーボードが出てたら閉じる
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference answerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);

        Map<String, String> data = new HashMap<String, String>();

        // UID
        data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        // 表示名
        // Preferenceから名前を取る
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        data.put("name", name);

        // 回答を取得する
        String answer = mAnswerEditText.getText().toString();

        if (answer.length() == 0) {
            // 回答が入力されていない時はエラーを表示するだけ
            Snackbar.make(v, "回答を入力して下さい", Snackbar.LENGTH_LONG).show();
            return;
        }
        data.put("body", answer);

        mProgress.show();
        answerRef.push().setValue(data, this);
    }

}

