package Controller;

import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import Activity.SecurityActivity;

public class SendEmailTask extends AsyncTask<Void, Void, Boolean> { // Thay đổi Void thành Boolean
    private WeakReference<SecurityActivity> activityReference;
    private String userEmail;
    private String senderEmail;
    private String password;
    private String verificationCode;

    public SendEmailTask(SecurityActivity context, String userEmail, String senderEmail, String password, String verificationCode) {
        activityReference = new WeakReference<>(context);
        this.userEmail = userEmail;
        this.senderEmail = senderEmail;
        this.password = password;
        this.verificationCode = verificationCode;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        SecurityActivity activity = activityReference.get();
        if (activity == null || activity.isFinishing()) {
            return null; // Nếu activity không tồn tại, không làm gì cả
        }

        try {
            // Cấu hình thông tin máy chủ email
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com"); // Máy chủ SMTP
            props.put("mail.smtp.port", "587"); // Cổng SMTP

            // Tạo phiên làm việc
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, password);
                }
            });

            // Tạo thông điệp email
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(userEmail));
            message.setSubject("Mã xác thực");
            message.setText("Mã xác thực của bạn là: " + verificationCode);

            // Gửi email
            Transport.send(message);
            return true; // Gửi email thành công
        } catch (Exception e) {
            e.printStackTrace();
            SecurityActivity securityActivity = activityReference.get();
            if (securityActivity != null) {
                securityActivity.runOnUiThread(() ->
                        Toast.makeText(securityActivity, "Gửi email thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
            return false; // Gửi email thất bại
        }
    }

    @Override
    protected void onPostExecute(Boolean success) { // Nhận đối số là Boolean
        SecurityActivity activity = activityReference.get();
        if (activity != null && !activity.isFinishing()) {
            if (success) {
                Toast.makeText(activity, "Mã xác thực đã được gửi đến email của bạn.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
