package Activity;

import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.capstone_app.R;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_privacy_policy);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.PrivacyPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Cấu hình WebView và tải nội dung chính sách quyền riêng tư
        WebView webView = findViewById(R.id.webViewPrivacyPolicy);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        Button btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(v -> finish());
        // Nội dung HTML của chính sách quyền riêng tư
        String htmlContent = "<html><body style='padding:16px;'>"
                + "<h1>Chính Sách Quyền Riêng Tư</h1>"
                + "<p>Chúng tôi cam kết bảo vệ quyền riêng tư của bạn. Thông tin của bạn sẽ không được chia sẻ với bên thứ ba mà không có sự đồng ý của bạn.</p>"
                + "<h2>Thông Tin Chúng Tôi Thu Thập</h2>"
                + "<p>Chúng tôi thu thập các thông tin cơ bản để cung cấp dịch vụ tốt hơn, bao gồm các thông tin cá nhân và thông tin hoạt động của bạn.</p>"
                + "<h2>Cách Sử Dụng Thông Tin</h2>"
                + "<p>Thông tin của bạn sẽ được sử dụng để cải thiện trải nghiệm người dùng, cung cấp các dịch vụ và hỗ trợ người dùng hiệu quả hơn.</p>"
                + "</body></html>";

        // Tải nội dung HTML vào WebView
        webView.loadData(htmlContent, "text/html", "UTF-8");
    }
}
