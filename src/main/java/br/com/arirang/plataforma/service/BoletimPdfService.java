package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.BoletimDTO;
import br.com.arirang.plataforma.dto.NotaDTO;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class BoletimPdfService {

    public byte[] gerarPdf(BoletimDTO boletim) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();

        // Configurar fonte padrão
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.DARK_GRAY);

        // Cabeçalho
        Paragraph title = new Paragraph("AriranG - Centro de Idiomas", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph subTitle = new Paragraph("Boletim de Notas", headerFont);
        subTitle.setAlignment(Element.ALIGN_CENTER);
        subTitle.setSpacingAfter(18);
        document.add(subTitle);

        // Informações do Aluno
        Paragraph alunoInfo = new Paragraph("Informações do Aluno", headerFont);
        alunoInfo.setSpacingAfter(10);
        document.add(alunoInfo);

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});

        addCell(infoTable, "Nome Completo:", normalFont);
        addCell(infoTable, boletim.alunoNome() != null ? boletim.alunoNome() : "N/A", normalFont);
        
        addCell(infoTable, "Turma:", normalFont);
        addCell(infoTable, boletim.turmaNome() != null ? boletim.turmaNome() : "N/A", normalFont);
        
        if (boletim.dataLancamento() != null) {
            addCell(infoTable, "Data de Lançamento:", normalFont);
            addCell(infoTable, boletim.dataLancamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), normalFont);
        }

        document.add(infoTable);
        document.add(new Paragraph(" ")); // Espaço

        // Tabela de Notas
        if (boletim.notas() != null && !boletim.notas().isEmpty()) {
            Paragraph notasTitle = new Paragraph("Notas Lançadas", headerFont);
            notasTitle.setSpacingAfter(10);
            document.add(notasTitle);

            PdfPTable notasTable = new PdfPTable(3);
            notasTable.setWidthPercentage(100);
            notasTable.setWidths(new float[]{2, 3, 1});

            // Cabeçalho da tabela
            addHeaderCell(notasTable, "Tipo", headerFont);
            addHeaderCell(notasTable, "Descrição", headerFont);
            addHeaderCell(notasTable, "Nota", headerFont);

            // Dados das notas
            for (NotaDTO nota : boletim.notas()) {
                String tipoNotaDesc = "N/A";
                if (nota.tipoNota() != null) {
                    tipoNotaDesc = nota.tipoNota().getDescricao() != null ? nota.tipoNota().getDescricao() : nota.tipoNota().name();
                }
                addCell(notasTable, tipoNotaDesc, normalFont);
                addCell(notasTable, nota.descricao() != null ? nota.descricao() : "N/A", normalFont);
                addCell(notasTable, nota.valorNota() != null ? String.valueOf(nota.valorNota()) : "N/A", normalFont);
            }

            document.add(notasTable);
            document.add(new Paragraph(" ")); // Espaço
        }

        // Média Final
        Paragraph mediaTitle = new Paragraph("Média Final", headerFont);
        mediaTitle.setSpacingAfter(10);
        document.add(mediaTitle);

        if (boletim.mediaFinal() != null) {
            Font mediaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Paragraph media = new Paragraph(String.format("%.2f", boletim.mediaFinal()), mediaFont);
            media.setAlignment(Element.ALIGN_CENTER);
            media.setSpacingAfter(10);
            document.add(media);
        }

        // Situação Final
        if (boletim.situacaoFinal() != null) {
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, 
                    "APROVADO".equals(boletim.situacaoFinal()) ? Color.GREEN : Color.RED);
            Paragraph situacao = new Paragraph(boletim.situacaoFinal(), statusFont);
            situacao.setAlignment(Element.ALIGN_CENTER);
            situacao.setSpacingAfter(20);
            document.add(situacao);
        }

        // Assinaturas
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{1, 1});

        PdfPCell cell1 = new PdfPCell(new Phrase("INSTITUTO ARIRANG / PROFESSOR RESPONSÁVEL", smallFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell1.setPaddingTop(40);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        signatureTable.addCell(cell1);

        PdfPCell cell2 = new PdfPCell(new Phrase("ALUNO (OU RESPONSÁVEL)", smallFont));
        cell2.setBorder(Rectangle.NO_BORDER);
        cell2.setPaddingTop(40);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        signatureTable.addCell(cell2);

        document.add(signatureTable);

        // Rodapé
        Paragraph footer = new Paragraph(
                "Este documento foi gerado automaticamente pelo sistema AriranG em " + 
                java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();

        return baos.toByteArray();
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        cell.setBorder(Rectangle.BOX);
        table.addCell(cell);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        cell.setBackgroundColor(Color.LIGHT_GRAY);
        cell.setBorder(Rectangle.BOX);
        table.addCell(cell);
    }
}

