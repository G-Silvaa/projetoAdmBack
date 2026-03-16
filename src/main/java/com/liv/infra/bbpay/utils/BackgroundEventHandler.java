package com.liv.infra.bbpay.utils;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;

public class BackgroundEventHandler implements IEventHandler {
    protected Image img;

    public BackgroundEventHandler(Image img) {
        this.img = img;
    }

    // Imagem de fundo para todas as páginas
    @SuppressWarnings("resource")
    public void handleEvent(Event event) {

        PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
        PdfDocument pdfDoc = docEvent.getDocument();
        PdfPage page = docEvent.getPage();
        PdfCanvas canvas = new PdfCanvas(page.newContentStreamBefore(),
                page.getResources(), pdfDoc);
        Rectangle area = page.getPageSize();
        try (Canvas c = new Canvas(canvas, pdfDoc, area)
                .add(img)) {
        }
    }

    // Imagem de fundo para uma página individual
    public void manipulatePdf(byte[] image, PdfDocument pdfDoc, PageSize pageSize, int page) {
        PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(page));
        canvas.addImage(ImageDataFactory.create(image), pageSize, false);
    }
}
