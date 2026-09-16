package com.example.sitiopro.criacao.aves.service;

import com.example.sitiopro.criacao.aves.dto.FichaOvoscopiaIncubacaoAves;
import com.example.sitiopro.criacao.aves.dto.OvoIncubacaoAvesResumo;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Service
public class FichaOvoscopiaPdfService {
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final PDType1Font REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public byte[] gerar(FichaOvoscopiaIncubacaoAves ficha) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int indice = 0;
            while (indice < ficha.ovos().size() || indice == 0) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    float y = cabecalho(content, ficha);
                    while (indice < ficha.ovos().size() && y >= 90) {
                        OvoIncubacaoAvesResumo ovo = ficha.ovos().get(indice++);
                        boolean pendente = ovo.pendenteReavaliacao();
                        texto(content, pendente ? BOLD : REGULAR, 9, 50, y, ovo.rotulo());
                        texto(content, REGULAR, 9, 112, y, "[  ]");
                        texto(content, REGULAR, 9, 176, y, "[  ]");
                        texto(content, REGULAR, 9, 248, y, "[  ]");
                        texto(content, REGULAR, 9, 320, y, "[  ]");
                        texto(content, REGULAR, 9, 380, y, pendente ? "Pendente de reavaliação" : "");
                        y -= 14;
                    }
                    if (indice >= ficha.ovos().size()) {
                        y -= 14;
                        texto(content, BOLD, 10, 50, y, "Resumo:");
                        y -= 28;
                        texto(content, REGULAR, 10, 50, y,
                                "Responsável: ________________________________________________");
                    }
                }
            }
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new AvesOperacaoException("PDF_OVOSCOPIA_FALHOU", "Não foi possível gerar a ficha de ovoscopia.");
        }
    }

    private float cabecalho(PDPageContentStream content, FichaOvoscopiaIncubacaoAves ficha) throws IOException {
        float y = 800;
        texto(content, BOLD, 16, 50, y, "Ficha de ovoscopia - " + ficha.propriedade());
        y -= 28;
        texto(content, REGULAR, 10, 50, y, "Incubação: " + ficha.codigoIncubacao()
                + " | Método: " + ficha.metodo().getRotulo()
                + " | Início: " + DATA.format(ficha.dataInicio()));
        y -= 15;
        texto(content, REGULAR, 10, 50, y, "Dia da incubação: " + ficha.diaIncubacao()
                + " | Data da ovoscopia: " + DATA.format(ficha.dataOvoscopia())
                + " | Próxima verificação: " + DATA.format(ficha.proximaVerificacao()));
        y -= 26;
        texto(content, BOLD, 9, 50, y, "Ovo");
        texto(content, BOLD, 9, 105, y, "Desenv.");
        texto(content, BOLD, 9, 165, y, "Rachadura");
        texto(content, BOLD, 9, 235, y, "Reavaliar");
        texto(content, BOLD, 9, 310, y, "Retirado");
        texto(content, BOLD, 9, 380, y, "Observação");
        return y - 12;
    }

    private void texto(PDPageContentStream content, PDType1Font font, int tamanho, float x, float y, String valor)
            throws IOException {
        content.beginText();
        content.setFont(font, tamanho);
        content.newLineAtOffset(x, y);
        content.showText(valor == null ? "" : valor);
        content.endText();
    }
}
