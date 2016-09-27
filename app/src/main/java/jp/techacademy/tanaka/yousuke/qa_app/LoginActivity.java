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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText;
    EditText mPasswordEditText;
    EditText mNameEditText;
    ProgressDialog mProgress;

    // Firebase関連はFirebaseAuthクラスと、
    // 処理の完了を受け取るリスナーであるOnCompleteListenerクラスをアカウント作成処理とログイン処理用の2つ、
    // そしてデータベースへの読み書きに必要なDatabaseReferenceクラスを定義します。
    FirebaseAuth mAuth;
    OnCompleteListener<AuthResult> mCreateAccountListener;
    OnCompleteListener<AuthResult> mLoginListener;
    DatabaseReference mDataBaseReference;

    // アカウント作成時にフラグを立て、ログイン処理後に名前をFirebaseに保存する
    boolean mIsCreateAccount = false;

    @Override
    /**
     * 以下の処理を実行
     * ・データベースへのリファレンスを取得
     * ・FirebaseAuthクラスのインスタンスを取得
     * ・アカウント作成処理のリスナーを作成
     * ・ログイン処理処理のリスナーを作成
     * ・タイトルバーのタイトルを変更
     * ・UIをメンバ変数に保持
     * ・アカウント作成ボタンとログインボタンのOnClickListenerを設定
     *
     *
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        // ----------------------------------------------------
        // Firebaseのアカウント作成処理はOnCompleteListenerクラスで受け取ります。
        // このクラスはonCompleteメソッドをオーバーライドする必要があります。
        // その中で引数で渡ってきたTaskクラスのisSuccessfulメソッドで成功したかどうかを確認します。
        // アカウント作成が成功した際にはそのままログイン処理を行うため、loginメソッドを呼び出します。
        // アカウント作成に失敗した場合は、Snackbarでエラーの旨を表示し、処理中に表示していたダイアログを非表示にします。

        // FirebaseAuthのオブジェクトを取得する
        mAuth = FirebaseAuth.getInstance();

        // アカウント作成処理のリスナー
        mCreateAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // 成功した場合
                    // ログインを行う
                    String email = mEmailEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    login(email, password);
                } else {

                    // 失敗した場合
                    // エラーを表示する
                    String errMsg = "アカウント作成に失敗しました";
                    if(task.getException() != null) {
                        errMsg = task.getException().getMessage();
                    }

                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, errMsg, Snackbar.LENGTH_LONG).show();

                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };


        // ----------------------------------------------------
        // Firebaseのログイン処理もOnCompleteListenerクラスで受け取ります。
        // ログインに成功したときはmIsCreateAccountを使ってアカウント作成ボタンを押してからのログイン処理か、
        // ログインボタンをタップの場合かで処理を分けます。
        //
        // アカウント作成ボタンを押した場合は表示名をFirebaseとPreferenceに保存します。
        //
        // ログインボタンをタップしたときは、Firebaseから表示名を取得してPreferenceに保存します。
        //
        // Firebaseからデータを一度だけ取得する場合は
        // DatabaseReferenceクラスが実装しているQueryクラスのaddListenerForSingleValueEventメソッドを使います。
        // ログインに失敗した場合は、Snackbarでエラーの旨を表示し、処理中に表示していたダイアログを非表示にします。

        // ログイン処理のリスナー
        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // 成功した場合
                    FirebaseUser user = mAuth.getCurrentUser();
                    DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());

                    if (mIsCreateAccount) {
                        // アカウント作成の時は表示名をFirebaseに保存する
                        String name = mNameEditText.getText().toString();


                        Map<String, String> data = new HashMap<String, String>();
                        data.put("name", name);
                        userRef.setValue(data);

                        // 表示名をPrefarenceに保存する
                        saveName(name);
                    } else {
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                Map data = (Map) snapshot.getValue();
                                saveName((String)data.get("name"));
                                saveFavoriteQuestionUid((HashMap)data.get(Const.FavoQUid));
                            }
                            @Override
                            public void onCancelled(DatabaseError firebaseError) {
                            }
                        });
                    }

                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();

                    // Activityを閉じる
                    finish();

                } else {
                    // 失敗した場合
                    // エラーを表示する
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();

                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };




        // ----------------------------------------------------
        // UI関連はタイトルの設定と、各UIのインスタンスをメンバ変数に保持、及びボタンのOnClickListnerの設定です。
        // アカウント作成ボタンをタップした時には、キーボードを閉じ、ログイン時に表示名を保存するようにmIsCreateAccountにtrueを設定します。
        // そしてcreateAccountメソッドを呼び出してアカウント作成処理を開始させます。
        // ログインボタンのタップした時にはキーボードを閉じ、loginメソッドを呼び出してログイン処理を開始させます。


        // UIの準備
        setTitle("ログイン");

        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);
        mNameEditText = (EditText) findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6 && name.length() != 0) {
                    // ログイン時に表示名を保存するようにフラグを立てる
                    mIsCreateAccount = true;

                    createAccount(email, password);
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                if (email.length() != 0 && password.length() >= 6) {
                    // フラグを落としておく
                    mIsCreateAccount = false;

                    login(email, password);
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * アカウント作成を行うcreateAccountメソッドではProgressDialogクラスのshowメソッドを呼び出してダイアログを表示させ、
     * FirebaseAuthクラスのcreateUserWithEmailAndPasswordメソッドでアカウント作成を行います。
     * createUserWithEmailAndPasswordメソッドの引数にはメールアドレス、パスワードを与え、さらにaddOnCompleteListenerメソッドを呼び出してリスナーを設定します。
     * @param email
     * @param password
     */
    private void createAccount(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();

        // アカウントを作成する
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);
    }

    /**
     * ログイン処理を行うloginメソッドではProgressDialogクラスのshowメソッドを呼び出してダイアログを表示させ、
     * FirebaseAuthクラスのsignInWithEmailAndPasswordメソッドでログイン処理を行います。
     * signInWithEmailAndPasswordメソッドの引数にはメールアドレス、パスワードを与え、さらにaddOnCompleteListenerメソッドを呼び出してリスナーを設定します。
     * @param email
     * @param password
     */
    private void login(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();

        // ログインする
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);
    }

    /**
     * saveNameメソッドでは引数で受け取った表示名をPreferenceに保存します。
     * 忘れずにcommitメソッドを呼び出して保存処理を反映させます。
     * @param name
     */
    private void saveName(String name) {
        // Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKEY, name);
        editor.commit();
    }

    /**
     * お気に入り情報をPreferenceに保存
     * @param fqUids お気に入りQUID一覧
     */
    private void saveFavoriteQuestionUid(HashMap fqUids) {
        // Preferenceに保存する
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        // 読み込んでSetの末尾に追加
        Set<String> uidSet = new HashSet<>();
        for (Object key : fqUids.keySet()) {
            uidSet.add((String)key);
        }

        // 保存
        SharedPreferences.Editor editor = sp.edit();
        editor.putStringSet(Const.FavoQUid, uidSet);
        editor.commit();
    }
}