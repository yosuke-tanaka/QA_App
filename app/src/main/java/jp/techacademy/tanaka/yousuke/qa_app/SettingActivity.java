package jp.techacademy.tanaka.yousuke.qa_app;

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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingActivity extends AppCompatActivity {

    DatabaseReference mDataBaseReference;
    private EditText mNameText;

    @Override
    /**
     * Preferenceから表示名を取得してEditTextに反映させる
     * 表示名変更ボタンとログアウトボタンのOnClickListenerを設定
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Preferenceから表示名を取得してEditTextに反映させる
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        EditText nameText = (EditText) findViewById(R.id.nameText);
        nameText.setText(name);

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        // UIの初期設定
        setTitle("設定");
        mNameText = (EditText) findViewById(R.id.nameText);




        // ----------------------------------
        // 表示名変更ボタンのOnClickListenerではログインしているかどうかを確認し、
        // もしログインしていなければSnackBarでその旨を表示して、その後は何もしません。
        // ログインしていればFirebaseに表示名を保存し、Preferenceにも保存します。
        // これらの処理はログイン画面での処理を同じ内容です。
        Button changeButton = (Button) findViewById(R.id.changeButton);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出ていたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていない場合は何もしない
                    Snackbar.make(v, "ログインしていません", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // 変更した表示名をFirebaseに保存する
                String name = mNameText.getText().toString();
                DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());
                Map<String, String> data = new HashMap<String, String>();
                data.put("name", name);
                userRef.setValue(data);

                // 変更した表示名をPreferenceに保存する
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Const.NameKEY, name);
                editor.commit();

                Snackbar.make(v, "表示名を変更しました", Snackbar.LENGTH_LONG).show();
            }
        });




        // ----------------------------------
        // ログアウトボタンのOnClickListenerではログアウト処理を行います。
        // ログアウトはFirebaseAuthクラスのsignOutメソッドを呼び出します。
        // signOutメソッドを呼び出した後はPreferenceに空文字(““)を保存し、Snackbarでログアウト完了の旨を表示します。
        Button logoutButton = (Button) findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                mNameText.setText("");
                // お気に入り情報をPreferenceから削除
                delFavoriteQuestionUid();
                Snackbar.make(v, "ログアウトしました", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    /**
     * お気に入り情報をPreferenceから削除
     */
    private void delFavoriteQuestionUid() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(Const.FavoQUid);
        editor.commit();
    }
}