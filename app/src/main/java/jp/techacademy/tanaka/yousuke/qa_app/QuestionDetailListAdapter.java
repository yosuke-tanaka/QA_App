package jp.techacademy.tanaka.yousuke.qa_app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 今まで作成したListViewとはレイアウトファイルを2つ使うという点で異なっています。
 * 今回の方法を使えば2つだけでなく3つ、4つとリストの各行に異なるレイアウトで表示させることが可能となります。
 *
 *
 *
 */
public class QuestionDetailListAdapter extends BaseAdapter {
    // どのレイアウトを使って表示させるかを判断するためのタイプを表す定数です。
    // 今回は質問と回答の2つのレイアウトを使うので2つの定数を用意しています。
    // もしこれが3つであればもう1つ定数を用意することとなります。
    private final static int TYPE_QUESTION = 0;
    private final static int TYPE_ANSWER = 1;

    private LayoutInflater mLayoutInflater = null;
    private Question mQustion;

    public QuestionDetailListAdapter(Context context, Question question) {
        mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mQustion = question;
    }

    @Override
    public int getCount() {
        return 1 + mQustion.getAnswers().size();
    }

    @Override
    /**
     * getItemViewTypeメソッドをオーバーライドしている点です。これは今までオーバーライドしていなかったメソッドです。
     * このメソッドは引数で渡ってきたポジションがどのタイプかを返すようにします。
     * 1行目、つまりポジションが0の時に質問であるTYPE_QUESTIONを返し、それ以外は回答なのでTYPE_ANSWERを返すようにします。
     */
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_QUESTION;
        } else {
            return TYPE_ANSWER;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public Object getItem(int position) {
        return mQustion;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    /**
     * getViewメソッドの中でgetItemViewTypeメソッドを呼び出してどちらのタイプかを判断してレイアウトファイルを指定します。
     *
     */
    public View getView(int position, View convertView, ViewGroup parent) {

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_question_detail, parent, false);
            }
            String body = mQustion.getBody();
            String name = mQustion.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);

            byte[] bytes = mQustion.getImageBytes();
            if (bytes.length != 0) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length).copy(Bitmap.Config.ARGB_8888, true);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                imageView.setImageBitmap(image);
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.list_answer, parent, false);
            }

            Answer answer = mQustion.getAnswers().get(position - 1);
            String body = answer.getBody();
            String name = answer.getName();

            TextView bodyTextView = (TextView) convertView.findViewById(R.id.bodyTextView);
            bodyTextView.setText(body);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
            nameTextView.setText(name);
        }

        return convertView;
    }
}