package it.release.petmanagment.ui.Customers.Pet;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.petmanagment.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FinalPetActivity extends AppCompatActivity {

    StorageReference storageRef;
    String id;
    String info;
    String infocustomer;
    FirebaseUser user;
    FirebaseFirestore db;
    FirebaseStorage storage;
    String name, race, typology;

    Bitmap bmp, scaledbmp;
    int pageWidth = 1200;

    ImageView imPet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_pet);
        imPet = findViewById(R.id.impet);
        ImageView imCam = findViewById(R.id.imcam);
        TextView petName = findViewById(R.id.pet_name);
        TextView petInfoLink = findViewById(R.id.pet_info);
        EditText petInfo = findViewById(R.id.petdata);
        Button generatePDFbtn = findViewById(R.id.pet_PDF_btn);
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_dog_icon);
        scaledbmp = Bitmap.createScaledBitmap(bmp, 1200, 518, false);

        Bundle extrasCustomer = getIntent().getExtras();
        infocustomer = extrasCustomer.getString("CustomerName");
        System.out.println(infocustomer);


        petInfo.setCursorVisible(false);
        petInfo.setVisibility(View.INVISIBLE);

        Bundle extras = getIntent().getExtras();
        info = extras.getString("Name");
        petName.setText(info);

        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();

        db.collection(user.getEmail()).document(infocustomer).collection(infocustomer).document(info).get().addOnSuccessListener(documentSnapshot -> {
            String content;

            id = documentSnapshot.getString("uuid");
            name = documentSnapshot.getString("name");
            race = documentSnapshot.getString("race");
            typology = documentSnapshot.getString("typology");

            content = String.format("Name: %s\nAnimal: %s\nRace: %s\n", name, typology, race);
            petInfo.setText(content);

            assert id != null;

            storageRef.child(id).getDownloadUrl().addOnSuccessListener(uri -> {
                imCam.setVisibility(View.INVISIBLE);
                Glide
                        .with(getApplicationContext())
                        .load(uri)
                        .into(imPet);
            }).addOnFailureListener(e -> System.out.println("no image"));
        });

        imPet.setOnClickListener(view -> {
            Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(gallery, 3);
            imCam.setVisibility(View.INVISIBLE);
        });

        petInfoLink.setOnClickListener(view -> {
            if (petInfo.getVisibility() == View.VISIBLE)
                petInfo.setVisibility(View.INVISIBLE);
            else
                petInfo.setVisibility(View.VISIBLE);
        });

        generatePDFbtn.setOnClickListener(view -> {
            createPDF();
            Toast.makeText(getApplicationContext(),"PDF created",Toast.LENGTH_LONG).show();
        });


    }

    private void createPDF() {
        PdfDocument myPdfDocument = new PdfDocument();
        Paint myPaint = new Paint();
        Paint titlePaint = new Paint();
        PdfDocument.PageInfo myPageInfo1 = new PdfDocument.PageInfo.Builder(1200, 2010, 1).create();
        PdfDocument.Page myPage1 = myPdfDocument.startPage(myPageInfo1);
        Canvas canvas = myPage1.getCanvas();

        canvas.drawBitmap(scaledbmp, 0, 0, myPaint);

        titlePaint.setTextAlign(Paint.Align.RIGHT);
        titlePaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        titlePaint.setTextSize(70);
        titlePaint.setColor(Color.rgb(0, 0, 0));
        canvas.drawText("Pet Management", pageWidth / 2, 270, titlePaint);
        myPaint.setTextAlign(Paint.Align.LEFT);
        myPaint.setTextSize(35);
        myPaint.setColor(Color.BLACK);
        canvas.drawText("Pet's name: " + name.toString(), 20, 590, myPaint);
        canvas.drawText("Pet: " + typology + " " + race, 20, 640, myPaint);


        myPdfDocument.finishPage(myPage1);

        File file = new File(Environment.getExternalStorageDirectory(), "/ciao.pdf");
        try {
            myPdfDocument.writeTo(new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

        myPdfDocument.close();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            imPet.setImageURI(selectedImage);
            StorageReference imageRef = storageRef.child(id);
            UploadTask imageLoader = (UploadTask) imageRef.putFile(selectedImage).addOnSuccessListener(taskSnapshot -> Toast.makeText(FinalPetActivity.this, "Upload ok", Toast.LENGTH_LONG))
                    .addOnFailureListener(e -> Toast.makeText(FinalPetActivity.this, "No upload", Toast.LENGTH_LONG));
        }
    }
}
/* TODO potrebbe andare bene per il pdf


import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

	// variables for our buttons.
	Button generatePDFbtn;

	// declaring width and height
	// for our PDF file.
	int pageHeight = 1120;
	int pagewidth = 792;

	// creating a bitmap variable
	// for storing our images
	Bitmap bmp, scaledbmp;

	// constant code for runtime permissions
	private static final int PERMISSION_REQUEST_CODE = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// initializing our variables.
		generatePDFbtn = findViewById(R.id.idBtnGeneratePDF);
		bmp = BitmapFactory.decodeResource(getResources(), R.drawable.gfgimage);
		scaledbmp = Bitmap.createScaledBitmap(bmp, 140, 140, false);

		// below code is used for
		// checking our permissions.
		if (checkPermission()) {
			Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
		} else {
			requestPermission();
		}

		generatePDFbtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// calling method to
				// generate our PDF file.
				generatePDF();
			}
		});
	}

	private void generatePDF() {
		// creating an object variable
		// for our PDF document.
		PdfDocument pdfDocument = new PdfDocument();

		// two variables for paint "paint" is used
		// for drawing shapes and we will use "title"
		// for adding text in our PDF file.
		Paint paint = new Paint();
		Paint title = new Paint();

		// we are adding page info to our PDF file
		// in which we will be passing our pageWidth,
		// pageHeight and number of pages and after that
		// we are calling it to create our PDF.
		PdfDocument.PageInfo mypageInfo = new PdfDocument.PageInfo.Builder(pagewidth, pageHeight, 1).create();

		// below line is used for setting
		// start page for our PDF file.
		PdfDocument.Page myPage = pdfDocument.startPage(mypageInfo);

		// creating a variable for canvas
		// from our page of PDF.
		Canvas canvas = myPage.getCanvas();

		// below line is used to draw our image on our PDF file.
		// the first parameter of our drawbitmap method is
		// our bitmap
		// second parameter is position from left
		// third parameter is position from top and last
		// one is our variable for paint.
		canvas.drawBitmap(scaledbmp, 56, 40, paint);

		// below line is used for adding typeface for
		// our text which we will be adding in our PDF file.
		title.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));

		// below line is used for setting text size
		// which we will be displaying in our PDF file.
		title.setTextSize(15);

		// below line is sued for setting color
		// of our text inside our PDF file.
		title.setColor(ContextCompat.getColor(this, R.color.purple_200));

		// below line is used to draw text in our PDF file.
		// the first parameter is our text, second parameter
		// is position from start, third parameter is position from top
		// and then we are passing our variable of paint which is title.
		canvas.drawText("A portal for IT professionals.", 209, 100, title);
		canvas.drawText("Geeks for Geeks", 209, 80, title);

		// similarly we are creating another text and in this
		// we are aligning this text to center of our PDF file.
		title.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
		title.setColor(ContextCompat.getColor(this, R.color.purple_200));
		title.setTextSize(15);

		// below line is used for setting
		// our text to center of PDF.
		title.setTextAlign(Paint.Align.CENTER);
		canvas.drawText("This is sample document which we have created.", 396, 560, title);

		// after adding all attributes to our
		// PDF file we will be finishing our page.
		pdfDocument.finishPage(myPage);

		// below line is used to set the name of
		// our PDF file and its path.
		File file = new File(Environment.getExternalStorageDirectory(), "GFG.pdf");

		try {
			// after creating a file name we will
			// write our PDF file to that location.
			pdfDocument.writeTo(new FileOutputStream(file));

			// below line is to print toast message
			// on completion of PDF generation.
			Toast.makeText(MainActivity.this, "PDF file generated successfully.", Toast.LENGTH_SHORT).show();
		} catch (IOException e) {
			// below line is used
			// to handle error
			e.printStackTrace();
		}
		// after storing our pdf to that
		// location we are closing our PDF file.
		pdfDocument.close();
	}

	private boolean checkPermission() {
		// checking of permissions.
		int permission1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
		int permission2 = ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE);
		return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED;
	}

	private void requestPermission() {
		// requesting permissions if not provided.
		ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0) {

				// after requesting permissions we are showing
				// users a toast message of permission granted.
				boolean writeStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
				boolean readStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED;

				if (writeStorage && readStorage) {
					Toast.makeText(this, "Permission Granted..", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}
	}
}

*/