package com.liv.infra.bbpay.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.OverflowPropertyValue;
import com.itextpdf.layout.property.Property;
import com.itextpdf.layout.property.TextAlignment;

public class BoletoPdf {

	public static byte[] emitirBoleto(String codigoLinhaDigitavel, String textoCodigoBarras, String textoQRCode,
			String nomeSacadoCobranca, String numeroInscricaoSacadoCobranca, String nossoNumero,
			String dataEmissaoTituloCobranca, String dataVencimentoTituloCobranca,
			String valorOriginalTituloCobranca, String numeroDocumento, String nomeSacadorAvalistaTitulo,
			String numeroInscricaoSacadorAvalista, String codigoAceiteTituloCobranca,
			String indicadorPermissaoRecebimentoParcial, String textoMensagemBloquetoTitulo,
			String textoEnderecoSacadoCobranca, String nomeMunicipioSacadoCobranca,
			String siglaUnidadeFederacaoSacadoCobranca, String numeroCepSacadoCobranca)
			throws IOException, WriterException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfDocument pdf = new PdfDocument(new PdfWriter(baos));
		Document doc = new Document(pdf);

		adicionarImagemDeFundo(pdf);
		adicionarConteudo(doc, "<b>" + formatarLinhaDigitavel(codigoLinhaDigitavel) + "</b>", 17, 227, 562, 209, 300);
		adicionarConteudo(doc,
				"<b>" + nomeSacadoCobranca + " - CPF: "
						+ formatarCpfComZeros(Long.parseLong(numeroInscricaoSacadoCobranca)) + "</b>",
				13, 31, 400, 251, 300);
		adicionarConteudo(doc, "<b>" + "0003556926" + completarZerosEsquerda(nossoNumero) + "</b>", 13, 31, 400,
				281, 330);
		adicionarConteudo(doc, "<b>" + numeroDocumento + "</b>", 13, 138, 400, 281, 330);
		adicionarConteudo(doc, "<b>" + dataVencimentoTituloCobranca + "</b>", 13, 245, 400, 281, 330);
		adicionarConteudo(doc, "<b>R$ " + valorOriginalTituloCobranca + "</b>", 13, 353, 450, 281, 330);
		adicionarConteudo(doc,
				"<b>" + nomeSacadorAvalistaTitulo + "/"
						+ formatarCnpjComZeros(Long.parseLong(numeroInscricaoSacadorAvalista)) + "</b>",
				13, 31, 500, 311, 350);
		adicionarConteudo(doc, "<b>" + dataVencimentoTituloCobranca + "</b>", 13, 433, 510, 491, 570);
		adicionarConteudo(doc, "<b>" + formatarLinhaDigitavel(codigoLinhaDigitavel) + "</b>", 17, 227, 562, 450, 500);
		adicionarConteudo(doc,
				"<b>" + nomeSacadorAvalistaTitulo + "/"
						+ formatarCnpjComZeros(Long.parseLong(numeroInscricaoSacadorAvalista)) + "</b>",
				13, 31, 421, 521, 600);
		adicionarConteudo(doc, "<b>" + dataEmissaoTituloCobranca + "</b>", 13, 31, 510, 560, 650);
		adicionarConteudo(doc, "<b>" + numeroDocumento + "</b>", 13, 117, 510, 560, 650);
		adicionarConteudo(doc, "<b>DM</b>", 13, 235, 510, 560, 650);
		adicionarConteudo(doc, "<b>" + codigoAceiteTituloCobranca + "</b>", 13, 294, 510, 560, 650);
		adicionarConteudo(doc, "<b>" + "0003556926" + completarZerosEsquerda(nossoNumero) + "</b>", 13, 434, 550,
				560, 650);
		adicionarConteudo(doc, "<b>R$</b>", 13, 176, 510, 590, 700);
		adicionarConteudo(doc, "<b>" + indicadorPermissaoRecebimentoParcial + "</b>", 13, 235, 510, 590, 700);
		adicionarConteudo(doc, "<b>R$ " + valorOriginalTituloCobranca + "</b>", 13, 434, 550, 589, 700);
		adicionarConteudo(doc, textoMensagemBloquetoTitulo, 12, 31, 400, 611, 750);
		adicionarConteudo(doc,
				"<b>" + nomeSacadoCobranca + " - CPF: "
						+ formatarCpfComZeros(Long.parseLong(numeroInscricaoSacadoCobranca)) + "</b>",
				13, 31, 510, 706, 800);
		adicionarConteudo(doc, "<b>" + textoEnderecoSacadoCobranca + "</b>", 13, 31, 510, 717, 800);
		adicionarConteudo(doc,
				"<b>" + nomeMunicipioSacadoCobranca + " - " + siglaUnidadeFederacaoSacadoCobranca + ", CEP: "
						+ numeroCepSacadoCobranca + "</b>",
				13, 31, 510, 728, 800);

		byte[] codigoBarras = gerarCodigoDeBarras(textoCodigoBarras);
		byte[] qrCode = gerarQRCode(textoQRCode);
		doc.add(adicionarImagem(qrCode, 1, 842f, 208, 174, 89, 89));
		doc.add(adicionarImagem(codigoBarras, 1, 842f, 29, 820, 305, 40));

		doc.close();
		return baos.toByteArray();
	}

	private static void adicionarConteudo(Document docu, String conteudo, Integer tamanhoFonte, float x1, float x2,
			float y1, float y2) throws IOException {
		Paragraph paragraph = new Paragraph();
		String conteudoHtml = "<p style='font-size:" + tamanhoFonte + "px'>"
				+ "<font face='Times-Roman'>" + conteudo + "</font></p>";
		List<IElement> elements = HtmlConverter.convertToElements(conteudoHtml);

		for (IElement element : elements) {
			paragraph.add((Paragraph) element);
		}

		paragraph.setTextAlignment(TextAlignment.JUSTIFIED);
		paragraph.setProperty(Property.OVERFLOW_Y, OverflowPropertyValue.HIDDEN);
		paragraph.setFixedPosition(1, x1, 842 - y2, x2 - x1);
		paragraph.setHeight(y2 - y1);

		docu.add(paragraph);
	}

	private static void adicionarImagemDeFundo(PdfDocument pdf) throws IOException {
		pdf.addNewPage(new PageSize(595f, 842f));

		byte[] imgFundo = carregarImagemDeFundo();
		Image image = new Image(ImageDataFactory.create(imgFundo)).scaleAbsolute(595f, 842f).setFixedPosition(0, 0);
		BackgroundEventHandler handler = new BackgroundEventHandler(image);
		handler.manipulatePdf(imgFundo, pdf, new PageSize(595f, 842f), 1);
	}

	private static byte[] carregarImagemDeFundo() throws IOException {
		try (InputStream inputStream = BoletoPdf.class.getResourceAsStream("/static/img/background.jpeg")) {
			if (inputStream == null) {
				throw new IOException("Imagem de fundo do boleto não encontrada em /static/img/background.jpeg");
			}
			return inputStream.readAllBytes();
		}
	}

	public static byte[] pegarBytesDoArquivo(String filePath) throws IOException {
		File arquivo = new File(filePath);
		byte[] bytesDoArquivo = new byte[(int) arquivo.length()];

		try (FileInputStream fis = new FileInputStream(arquivo)) {
			fis.read(bytesDoArquivo);
		}

		return bytesDoArquivo;
	}

	public static byte[] gerarCodigoDeBarras(String dado) throws WriterException, IOException {
		Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 150 * (1 / 4));

		BitMatrix bitMatrix = new MultiFormatWriter().encode(dado, BarcodeFormat.CODE_128, 150, 150, hints);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
		return baos.toByteArray();
	}

	public static byte[] gerarQRCode(String dado) throws WriterException, IOException {
		Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
		hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		hints.put(EncodeHintType.MARGIN, 150 * (1 / 4));

		BitMatrix bitMatrix = new MultiFormatWriter().encode(dado, BarcodeFormat.QR_CODE, 250, 250, hints);

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
		return baos.toByteArray();
	}

	private static Image adicionarImagem(byte[] imagem, int pagina, float tamanhoPagina, float x, float y, int largura,
			int tamanho) {
		ImageData imageData = ImageDataFactory.create(imagem);
		return new Image(imageData).setFixedPosition(pagina, x, tamanhoPagina - y).setWidth(largura).setHeight(tamanho);
	}

	public static String formatarCpfComZeros(long cpf) {
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("00000000000");
		return df.format(cpf);
	}

	public static String formatarCnpjComZeros(long cnpj) {
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("00000000000000");
		return df.format(cnpj).replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
	}

	public static String completarZerosEsquerda(String numeroDocumento) {
		int tamanhoString = numeroDocumento.length();
		StringBuilder res = new StringBuilder();
		for (int i = tamanhoString; i < 10; i++) {
			res.append('0');
		}
		res.append(numeroDocumento);
		return res.toString();
	}

	public static String formatarLinhaDigitavel(String linhaDigitavel) {
		StringBuilder res = new StringBuilder();
		res.append(linhaDigitavel, 0, 5).append('.');
		res.append(linhaDigitavel, 5, 10).append(' ');
		res.append(linhaDigitavel, 10, 15).append('.');
		res.append(linhaDigitavel, 15, 21).append(' ');
		res.append(linhaDigitavel, 21, 26).append('.');
		res.append(linhaDigitavel, 26, 32).append(' ');
		res.append(linhaDigitavel, 32, 33).append(' ');
		res.append(linhaDigitavel.substring(33));
		return res.toString();
	}
}
