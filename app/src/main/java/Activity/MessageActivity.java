package Activity;

import static android.graphics.Color.BLACK;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.capstone_app.R;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MessageActivity extends AppCompatActivity {

    private EditText messageInput;
    private Button sendButton;
    private String recipientEmail = "nhatnhatnhatle@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        ImageButton backButton = findViewById(R.id.imageButtonBack);
        backButton.setOnClickListener(v -> finish());

        // Thiết lập listener cho nút gửi
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                String username = "xxxxx.com";
                String appPassword = "111111";

                new SendEmailTask(username, appPassword, message).execute();
            } else {
                Toast.makeText(MessageActivity.this, "Vui lòng nhập tin nhắn", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        private String username;
        private String appPassword;
        private String message;

        SendEmailTask(String username, String appPassword, String message) {
            this.username = username;
            this.appPassword = appPassword;
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            // Tạo phiên làm việc
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, appPassword);
                }
            });

            try {
                Message msg = new MimeMessage(session);
                msg.setFrom(new InternetAddress(username));
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                msg.setSubject("New Message");
                msg.setText(message);
                Transport.send(msg);
                return true;
            } catch (MessagingException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                Toast.makeText(MessageActivity.this, "Bạn đã gửi thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MessageActivity.this, "Gửi thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
            messageInput.setText("");
        }
    }
}
