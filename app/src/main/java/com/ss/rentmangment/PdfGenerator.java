package com.ss.rentmangment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class PdfGenerator {

    private static final String TAG = "PdfGenerator";
    private final Context context;

    public PdfGenerator(Context context) {
        this.context = context;
    }

    /**
     * Legacy method to generate a PDF and save it to a specific file path.
     * This is suitable for Android versions below 10 (API 29).
     * @param title The title of the report.
     * @param data The table data for the report.
     * @param file The file to save the PDF to.
     * @throws IOException If there is an error writing to the file.
     */
    public void generatePdf(String title, List<String[]> data, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            generatePdfToStream(title, data, fos);
        }
    }

    /**
     * Modern method to generate a PDF and write it to an OutputStream.
     * This is the preferred method for modern Android (API 29+) as it works with MediaStore.
     * @param title The title of the report.
     * @param data The table data for the report.
     * @param stream The OutputStream to write the PDF content to.
     * @throws IOException If there is an error writing to the stream.
     */
    public void generatePdfToStream(String title, List<String[]> data, OutputStream stream) throws IOException {
        // A4 page dimensions in points (1 point = 1/72 inch)
        final int PAGE_WIDTH = 595;
        final int PAGE_HEIGHT = 842;
        final int TOP_MARGIN = 40;
        final int LEFT_MARGIN = 40;
        final int RIGHT_MARGIN = 40;
        final int BOTTOM_MARGIN = 40;
        final int CONTENT_WIDTH = PAGE_WIDTH - LEFT_MARGIN - RIGHT_MARGIN;

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // --- Setup Paints ---
        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.BLACK);
        titlePaint.setTextSize(18f);
        titlePaint.setFakeBoldText(true);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        TextPaint headerPaint = new TextPaint();
        headerPaint.setColor(Color.WHITE);
        headerPaint.setTextSize(12f);
        headerPaint.setFakeBoldText(true);

        TextPaint cellPaint = new TextPaint();
        cellPaint.setColor(Color.BLACK);
        cellPaint.setTextSize(11f);

        // --- Draw Title ---
        int currentY = TOP_MARGIN + 20;
        canvas.drawText(title, PAGE_WIDTH / 2.0f, currentY, titlePaint);
        currentY += 40;

        // --- Handle Empty Data Case ---
        if (data == null || data.size() <= 1) {
            cellPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("No data available for this report.", PAGE_WIDTH / 2.0f, currentY + 50, cellPaint);
            document.finishPage(page);
            writeDocumentToStream(document, stream);
            return;
        }

        // --- Draw Table Header ---
        String[] headers = data.get(0);
        int numColumns = headers.length;
        float columnWidth = (float) CONTENT_WIDTH / numColumns;
        int rowHeight = 40; // Increased height for better text wrapping

        Paint headerBgPaint = new Paint();
        headerBgPaint.setColor(Color.parseColor("#424242")); // Dark Gray
        canvas.drawRect(LEFT_MARGIN, currentY, PAGE_WIDTH - RIGHT_MARGIN, currentY + rowHeight, headerBgPaint);

        for (int i = 0; i < numColumns; i++) {
            float currentX = LEFT_MARGIN + (i * columnWidth);
            drawTextWithWrapping(canvas, headers[i], currentX + 5, currentY + 5, (int)columnWidth - 10, headerPaint);
        }
        currentY += rowHeight;

        // --- Draw Table Rows ---
        for (int i = 1; i < data.size(); i++) {
            String[] row = data.get(i);

            // Check for page break
            if (currentY + rowHeight > PAGE_HEIGHT - BOTTOM_MARGIN) {
                document.finishPage(page);
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                currentY = TOP_MARGIN; // Reset Y for new page
            }

            // Alternate row background color for readability
            if (i % 2 != 0) {
                Paint rowBgPaint = new Paint();
                rowBgPaint.setColor(Color.parseColor("#F5F5F5")); // Light Gray
                canvas.drawRect(LEFT_MARGIN, currentY, PAGE_WIDTH - RIGHT_MARGIN, currentY + rowHeight, rowBgPaint);
            }

            for (int j = 0; j < row.length; j++) {
                float currentX = LEFT_MARGIN + (j * columnWidth);
                drawTextWithWrapping(canvas, row[j], currentX + 5, currentY + 5, (int)columnWidth - 10, cellPaint);
            }
            currentY += rowHeight;
        }

        document.finishPage(page);
        writeDocumentToStream(document, stream);
    }

    /**
     * Helper method to draw text that wraps within a specified width.
     * @param canvas The canvas to draw on.
     * @param text The text to draw.
     * @param x The x-coordinate of the text box.
     * @param y The y-coordinate of the text box.
     * @param width The maximum width for the text.
     * @param paint The paint to use for drawing.
     */
    private void drawTextWithWrapping(Canvas canvas, String text, float x, float y, int width, TextPaint paint) {
        if (text == null) return;
        canvas.save();
        canvas.translate(x, y);
        StaticLayout staticLayout = new StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * Writes the finished PdfDocument to the given OutputStream and closes the document.
     * @param document The PdfDocument to write.
     * @param stream The OutputStream to write to.
     * @throws IOException If an error occurs during writing.
     */
    private void writeDocumentToStream(PdfDocument document, OutputStream stream) throws IOException {
        try {
            document.writeTo(stream);
        } finally {
            document.close();
            Log.d(TAG, "PdfDocument has been written and closed.");
        }
    }
}
