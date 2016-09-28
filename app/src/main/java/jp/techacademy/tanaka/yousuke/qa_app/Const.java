package jp.techacademy.tanaka.yousuke.qa_app;

public class Const {
    public static final String UsersPATH = "users"; // Firebaseにユーザの表示名を保存するパス
    public static final String ContentsPATH = "contents"; // Firebaseに質問を保存するバス
    public static final String AnswersPATH = "answers"; // Firebaseに解答を保存するパス

    public static final String NameKEY = "name"; // Preferenceに表示名を保存する時のキー

    // 2016.09.20 [修正] お気に入り追加
    public static final String FavoQUid = "fquid"; // お気に入り質問のUIDを保存する時のキー
}