package com.example.pdf_itextpdf;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.AreaBreakType;
import com.itextpdf.layout.property.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText etTimeDate;
    private Button btnPDF;

    private static final String TAG = "PdfCreatorActivity";
    public final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnPDF = (Button) findViewById(R.id.btnPDF);
        etTimeDate = (EditText) findViewById(R.id.etTimeDate);

        // Edit text tanggal untuk etTglBerangkat
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                updateCalender();
            }

            private void updateCalender() {
                // Mengatur format yang akan tampil di EditText untuk tanggal Periode
                String format = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.US);

                etTimeDate.setText(sdf.format(calendar.getTime()));
            }
        };

        etTimeDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, date,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                datePickerDialog.show();
            }
        });

        btnPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etTimeDate.getText().toString().trim().length() == 0){
                    etTimeDate.setError("Tolong Diisi");
                }else {
                    etTimeDate.setError(null);
                    try {
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                                getApplicationContext(), WRITE_EXTERNAL_STORAGE) ==
                                PackageManager.PERMISSION_GRANTED)
                        {
                            createPdf();
                        }else {
                            requestAllPermission();
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void createPdf() throws FileNotFoundException {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/Tes6.pdf");
        OutputStream output = new FileOutputStream(file);

        PdfWriter pdfWriter = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(pdfWriter);
        Document document = new Document(pdfDocument);

        pdfDocument.setDefaultPageSize(PageSize.A4);

        // -------- Content --------

        // Cetak Gambar
        Drawable d = getDrawable(R.drawable.ic_apk_travelagent);
        Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();

        ImageData data = ImageDataFactory.create(bitmapData);
        Image image = new Image(data);
        image.scaleToFit(60, 60);

        //Cetak Tabel
        float columnWidth[] = {100f, 130f, 130f, 120f, 120f, 120f, 120f, 80f};
        Table table1 = new Table(columnWidth);

        //Tabel 1 --- 01
        table1.addCell(new Cell(3,1).add(image).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
        table1.addCell(new Cell(1,2).add(new Paragraph("Travel Agent").setFontSize(16).setTextAlignment(TextAlignment.LEFT).setBold().setUnderline()).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell(1,2).add(new Paragraph("Invoice").setFontSize(12).setTextAlignment(TextAlignment.LEFT).setBold().setUnderline()).setBorder(Border.NO_BORDER));
//        table1.addCell(new Cell().add(new Paragraph("")));

        //Tabel 1 --- 02
        table1.addCell(new Cell(1,5).add(new Paragraph("Jl Krakatau B.97, Kota Salatiga, Jawa Tengah (Kode Pos : 50732)").setFontSize(9).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
//        table1.addCell(new Cell().add(new Paragraph("")));
//        table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("Invoice No :").setFontSize(9).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("21897948201").setFontSize(9).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER));

        //Tabel 1 --- 03
        table1.addCell(new Cell(1,5).add(new Paragraph("Telepon : 085741844404    Email : travelagent@gmail.com").setFontSize(9).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
//        table1.addCell(new Cell().add(new Paragraph("")));
//        table1.addCell(new Cell().add(new Paragraph("")));
        table1.addCell(new Cell().add(new Paragraph("Invoice Date :").setFontSize(9).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));
        table1.addCell(new Cell().add(new Paragraph("11-05-2023").setFontSize(9).setTextAlignment(TextAlignment.LEFT)).setBorder(Border.NO_BORDER).setBorderBottom(new SolidBorder(1f)));

        //Tabel 1 --- 04
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        //Tabel 1 --- 05
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table1.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));


        Table table2 = new Table(columnWidth);

        //Tabel 2 --- 01
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1, 2).add(new Paragraph("Pemesan").setFontSize(10).setBold().setUnderline()).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell().add(new Paragraph("")));

        //Tabel 2 --- 02
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1,2).add(new Paragraph("Nama : Dewa Asmara Putra").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("Dewa Asmara Putra").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 03
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1,2).add(new Paragraph("Email : dewaasmara589@gmail.com").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("dewaasmara589@gmail.com").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 04
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1,2).add(new Paragraph("Nomor WA : 088229872059").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("088229872059").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 05
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1,2).add(new Paragraph("Tgl Berangkat : 23-07-2023").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("23-07-2023").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 06
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1, 2).add(new Paragraph("Jenis Paket : Jogja Eksekutif").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("Jogja Eksekutif").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 07
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1, 2).add(new Paragraph("Durasi Wisata : 3 Hari").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("3 Hari").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 08
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell(1, 2).add(new Paragraph("Jumlah Pax : 25 Hari").setFontSize(9)).setBorder(Border.NO_BORDER));
//        table2.addCell(new Cell(1, 2).add(new Paragraph("25 Hari").setFontSize(9)).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 09
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        //Tabel 2 --- 010
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        table2.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        document.add(table1);
        document.add(table2);

        // Cetak Tanggal
        int durasiWisata = 3;
        String tanggal = etTimeDate.getText().toString().trim();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
        for (int i = 0; i<durasiWisata; i++){
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTime(simpleDateFormat.parse(tanggal));
                cal.add(Calendar.DAY_OF_MONTH,i);

                String hasilTambahTanggal = String.valueOf(simpleDateFormat.format(cal.getTime()));

                Paragraph paragraph = new Paragraph(hasilTambahTanggal).setFontSize(14).setBold();

                document.add(paragraph);

                if (i<durasiWisata-1){
                    // Add next page PDF
                    document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                }

                Log.e(TAG, "Tanggal : " + simpleDateFormat.format(cal.getTime()).toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }



        // -------- END Content --------

        document.close();

        previewPdf();
    }

    private void previewPdf() {
        // Get the File location and file name.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "/Tes6.pdf");
        Log.d("pdfFIle", "" + file);

        // Get the URI Path of file.
        Uri uriPdfPath = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", file);
        Log.d("pdfPath", "" + uriPdfPath);

        // Start Intent to View PDF from the Installed Applications.
        Intent pdfOpenIntent = new Intent(Intent.ACTION_VIEW);
        pdfOpenIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        pdfOpenIntent.setClipData(ClipData.newRawUri("", uriPdfPath));
        pdfOpenIntent.setDataAndType(uriPdfPath, "application/pdf");
        pdfOpenIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |  Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {
            startActivity(pdfOpenIntent);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(this,"There is no app to load corresponding PDF",Toast.LENGTH_LONG).show();
        }
    }

    private void requestAllPermission(){
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{ READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();

                try {
                        createPdf();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}