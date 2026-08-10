package com.example.demo.util.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.example.demo.dto.response.BloodStockResponse;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class BloodStockPdfGenerator {

    private BloodStockPdfGenerator() {
    }

    public static ByteArrayInputStream generate(List<BloodStockResponse> stocks) {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Blood Stock Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 3, 2 });

            PdfPCell h1 = new PdfPCell(new Phrase("Blood Group"));
            PdfPCell h2 = new PdfPCell(new Phrase("Units"));

            table.addCell(h1);
            table.addCell(h2);

            for (BloodStockResponse stock : stocks) {
                table.addCell(stock.getBloodGroup() != null ? stock.getBloodGroup().getValue() : "N/A");
                table.addCell(String.valueOf(stock.getUnits() != null ? stock.getUnits() : 0));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

}
