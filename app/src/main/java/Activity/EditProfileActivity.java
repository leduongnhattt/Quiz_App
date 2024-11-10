package Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.capstone_app.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText editName, editEmail;
    private Button buttonSave, buttonCancel;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private Uri imageUri;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        if (currentUser != null) {
            String userId = currentUser.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        }

        // Khởi tạo Views
        editName = findViewById(R.id.edit_name);
        editEmail = findViewById(R.id.edit_email);
        profileImage = findViewById(R.id.profile_image);
        buttonSave = findViewById(R.id.button_save);
        buttonCancel = findViewById(R.id.button_cancel);

        // Load user data
        loadUserData();

        // Click listener cho image profile
        profileImage.setOnClickListener(v -> openGallery());

        // Click listener cho button lưu
        buttonSave.setOnClickListener(v -> saveUserData());

        // Click listener cho button hủy
        buttonCancel.setOnClickListener(v -> showCancelConfirmationDialog());
    }

    private void loadUserData() {
        if (databaseReference != null) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String name = dataSnapshot.child("name").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        editName.setText(name);
                        editEmail.setText(email);
                        String imageUrl = dataSnapshot.child("picture").getValue(String.class);
                        if (imageUrl != null) {
                            Glide.with(EditProfileActivity.this).load(imageUrl).into(profileImage);
                        } else {
                            profileImage.setImageResource(R.drawable.profile);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(EditProfileActivity.this, "Không thể tải dữ liệu người dùng.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    private void saveUserData() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();

        if (databaseReference != null) {
            databaseReference.child("name").setValue(name);
            databaseReference.child("email").setValue(email);

            if (imageUri != null) {
                StorageReference fileReference = storageReference.child(currentUser.getUid() + ".jpg");
                UploadTask uploadTask = fileReference.putFile(imageUri);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    // Lấy URL Download từ firebase storage
                    fileReference.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        // Lưu URL vào Realtime Database
                        databaseReference.child("picture").setValue(downloadUri.toString());
                        Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Không thể tải ảnh lên.", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showCancelConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy chỉnh sửa")
                .setMessage("Bạn có muốn thoát mà không lưu thay đổi không?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> finish())
                .setNegativeButton(android.R.string.no, null)
                .show();
    }
}
